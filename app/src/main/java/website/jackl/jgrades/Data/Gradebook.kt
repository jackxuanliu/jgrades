package website.jackl.jgrades.Data

import android.util.Log
import website.jackl.data_processor.Data
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Created by jack on 1/22/18.
 */
@Data data class Gradebook(val summary: Summary, val details: Details? = null, val lastDetailsCheck: Long = 0 /* milliseconds */, val lastView: Long?) {
    companion object {
        fun simplifyName(name: String): String {
            val parts = name.split("-")
            val base = parts[0]

            return base.trim()
        }

        fun convertTerm(termCode: String): String {

            when (termCode) {
                "F" -> return "Fall"
                "S" -> return "Spring"
                else -> return ""
            }
        }

        fun calculateCategoryTotals(assignments: List<Assignment>): Map<String, Pair<Double, Double>> {
            val map = mutableMapOf<String, Pair<Double, Double>>()

            for (assignment in assignments) {
                if (assignment.isGraded) {
                    assignment.apply {
                        val pair = map.getOrPut(category, { Pair(0.00, 0.00) })
                        map.put(category, pair.copy(pair.first + points, pair.second + maxPoints))
                    }
                }
            }

            return map
        }

        val decimalFormat: DecimalFormat
        get() {
            val format = DecimalFormat("###,###.##")
            format.roundingMode = RoundingMode.FLOOR
            return format
        }
        val percentFormat: DecimalFormat
            get() {
                val format = DecimalFormat("###,###.##%")
                format.roundingMode = RoundingMode.FLOOR
                return format
            }
        val classPercentFormat: DecimalFormat
            get() {
                val format = DecimalFormat("###,##0.00%")
                format.roundingMode = RoundingMode.FLOOR
                return format
            }
    }

    @Data data class Summary(val numberTerm: String, val name: String, val term: String, val officialPercent: Int, val officialMark: String, val code: String?, val lastUpdated: Long /* milliseconds */)

    @Data data class Details(val detailedSummaryData: DetailedSummaryData, val assignments: List<Assignment>) {
        fun calculateGrade(): Double {
            if (detailedSummaryData.maxPoints == 0.00) {
                var totalScore = 0.00
                var totalWeight = 0.00
                for (entry in detailedSummaryData.categories) {
                    val category = entry.value
                    if (category.maxPoints > 0.00) {
                        val score = (category.points / category.maxPoints) * category.weight
                        if (score != Double.NaN) {
                            totalScore += score
                            totalWeight += category.weight
                        }
                    }
                }
                if (totalWeight > 0.00) {
                    return totalScore / totalWeight
                } else {
                    return 0.00
                }
            } else {
                return detailedSummaryData.points / detailedSummaryData.maxPoints
            }
        }

        fun calculateGrade(newAssignments: List<Assignment>): Double {
            val originalCategoryTotals = calculateCategoryTotals(assignments)
            val newCategoryTotals = calculateCategoryTotals(newAssignments)

            var isPointBased = true

            val newCategories = mutableMapOf<String, Category>()

            for (entry in newCategoryTotals) {

                val name = entry.key
                val originalTotals = originalCategoryTotals.get(name) ?: Pair(0.0, 0.0)
                val newTotals = entry.value

                val originalCategory = detailedSummaryData.categories.get(name)!!

                newCategories.put(name,
                        originalCategory.copy(
                                points =    originalCategory.points + (newTotals.first - originalTotals.first),
                                maxPoints = originalCategory.maxPoints + (newTotals.second - originalTotals.second)
                        )
                )
                if ( detailedSummaryData.categories.get(name)!!.weight != 0.00) isPointBased = false


            }



            if (!isPointBased) {
                var totalScore = 0.00
                var totalWeight = 0.00
                for (entry in newCategories) {
                    val category = entry.value
                    if (category.maxPoints > 0.00) {
                        val score = (category.points / category.maxPoints) * category.weight
                        if (score != Double.NaN) {
                            totalScore += score
                            totalWeight += category.weight
                        }
                    }
                }
                if (totalWeight > 0.00) {
                    return totalScore / totalWeight
                } else {
                    return 0.00
                }
            } else {
                var points = 0.0
                var maxPoints = 0.0

                for (entry in newCategories) {
                    val category = entry.value
                    points += category.points
                    maxPoints += category.maxPoints
                }
                return points / maxPoints
            }
        }


    }

    @Data data class DetailedSummaryData(val points: Double, val maxPoints: Double, val categories: Map<String, Category>)

    @Data data class Assignment(val name: String, val category: String, val points: Double, val maxPoints: Double, val isGraded: Boolean)

    @Data data class Category(val name: String, val points: Double, val maxPoints: Double, val weight: Double)
}

