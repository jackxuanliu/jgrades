package website.jackl.jgrades.Data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import website.jackl.generated.data.*
import website.jackl.jgrades.Data.CollectionExtensions.getJSONObject
import website.jackl.jgrades.Data.CollectionExtensions.putJSONObject
import website.jackl.jgrades.fragment.defaultPrefs

/**
 * Created by jack on 12/19/17.
 */

class SettingsManager(val context: Context, val prefsName: String = "default") {

    fun exportGradebooks(): String {
        val gradebookMap = prefs.getJSONObject("gradebookMap") ?: JSONObject()
        return gradebookMap.toString(2)
    }

    fun loadGlobal(): Global {
        try {
            return constructGlobal(prefs.getJSONObject("global")!!)!!
        } catch (e: Throwable) {
            return Global()
        }
    }

    fun saveGlobal(value: Global) {
        prefsEdit!!.putJSONObject("global", value.write())
        prefsEdit!!.commit()
    }


    fun loadNotificationLocks(): NotificationLocks {
        try {
            return constructNotificationLocks(prefs.getJSONObject("notificationPrefs")!!)!!
        } catch (e: Throwable) {
            return NotificationLocks()
        }
    }

    fun saveNotificationLocks(value: NotificationLocks) {
        prefsEdit.putJSONObject("notificationPrefs", value.write())
        prefsEdit.commit()
    }

    fun loadUser(email: String): User {
        try {
            val userMap = prefs.getJSONObject("userMap")
            return constructUser(userMap!!.getJSONObject(email))!!
        } catch (e: Throwable) {
            return User(email)
        }
    }


    fun saveUser(user: User) {
        val userMap = prefs.getJSONObject("userMap") ?: JSONObject()
        userMap.put(user.email, user.write())
        prefsEdit.putJSONObject("userMap", userMap)
        prefsEdit.commit()
    }

    fun loadStudent(id: Student.Info): Student {
        try {
            val studentMap = prefs!!.getJSONObject("studentMap")

            return constructStudent(studentMap!!.getJSONObject(id.unique))!!
        } catch (e: Throwable) {
            return Student(id)
        }
    }

    fun loadAnyStudent(): Student? {
        try {
            val studentMap = prefs.getJSONObject("studentMap")

            for (key in studentMap!!.keys()) {
                return constructStudent(studentMap.getJSONObject(key))
            }

            return null
        } catch (e: Throwable) {
            return null
        }
    }

    fun deleteGradebookData() {
        prefsEdit.remove("gradebookMap").commit()
    }

    fun saveStudent(student: Student) {
        val studentMap = prefs!!.getJSONObject("studentMap") ?: JSONObject()
        studentMap.put(student.info.unique, student.write())
        prefsEdit.putJSONObject("studentMap", studentMap)
        prefsEdit.commit()
    }

    fun loadGradebook(id: Student.Info, numberTerm: String): Gradebook? {
        try {
            val gradebookMap = prefs.getJSONObject("gradebookMap") ?: JSONObject()
            val studentGradebooks = gradebookMap.optJSONObject(id.unique) ?: JSONObject()
            var gradebook: Gradebook? = null

            gradebook = constructGradebook(studentGradebooks.getJSONObject(numberTerm))

            return gradebook
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    fun saveGradebook(id: Student.Info, gradebook: Gradebook) {
        val gradebookMap = prefs.getJSONObject("gradebookMap") ?: JSONObject()
        val studentGradebooks = gradebookMap.optJSONObject(id.unique) ?: JSONObject()
        studentGradebooks.put(gradebook.summary.numberTerm, gradebook.write())
        gradebookMap.put(id.unique, studentGradebooks)

        prefsEdit.putJSONObject("gradebookMap", gradebookMap)
        prefsEdit.commit()
    }

    fun loadGradebookSummaries(id: Student.Info): List<Gradebook.Summary> {
        try {
            val summaries = mutableListOf<Gradebook.Summary>()

            val gradebookMap = prefs.getJSONObject("gradebookMap") ?: JSONObject()
            val studentGradebooks = gradebookMap.optJSONObject(id.unique) ?: JSONObject()

            for (key in studentGradebooks.keys().iterator()) {
                var gradebook: Gradebook? = null
                try {
                    gradebook = constructGradebook(studentGradebooks.getJSONObject(key))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                val summary = gradebook?.summary
                val showNotCurrent = context.defaultPrefs.getBoolean("pref_showPrior", false)
                if (summary != null) {
                    val isNotCurrent = summary.code != ""
                    if (isNotCurrent) {
                        if (showNotCurrent) {
                            summaries.add(summary)
                        } else {

                        }
                    } else {
                        summaries.add(summary)
                    }
                }
            }
            return summaries
        } catch (e: Throwable) {
            e.printStackTrace()
            return listOf()
        }
    }


    data class SummariesResult(val result: List<Gradebook.Summary>, val student: Student.Info?)

    fun loadAnyGradebookSummaries(): SummariesResult { // get first saved student and load summaries
        val summaries: MutableList<Gradebook.Summary> = mutableListOf()
        try {

            val student: Student.Info? = loadAnyStudent()?.info

            if (student != null) {
                summaries.addAll(loadGradebookSummaries(student))
            }

            return SummariesResult(summaries, student)
        } catch (e: Throwable) {
            e.printStackTrace()
            return SummariesResult(listOf(), null)
        }
    }

    fun saveGradebookSummaries(id: Student.Info, summaries: List<Gradebook.Summary>) {

        val gradebookMap = prefs.getJSONObject("gradebookMap") ?: JSONObject()
        val studentGradebooks = gradebookMap.optJSONObject(id.unique) ?: JSONObject()

        for (summary in summaries) {
            var gradebook: Gradebook? = null

            try {
                gradebook = constructGradebook(studentGradebooks.getJSONObject(summary.numberTerm))
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            if (gradebook != null)
                studentGradebooks.put(summary.numberTerm, gradebook.copy(summary = summary).write())
            else
                studentGradebooks.put(summary.numberTerm, Gradebook(summary, null, 0, summary.lastUpdated).write())
        }

        gradebookMap.put(id.unique, studentGradebooks)
        prefsEdit.putJSONObject("gradebookMap", gradebookMap)
        prefsEdit.commit()
    }

    private var prefs: SharedPreferences = context.getSharedPreferences(prefsName, 0)
    private var prefsEdit: SharedPreferences.Editor = prefs?.edit()

}