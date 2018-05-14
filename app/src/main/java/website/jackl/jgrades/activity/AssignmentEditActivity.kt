package website.jackl.jgrades.activity

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import org.json.JSONObject
import website.jackl.generated.data.constructGradebookAssignment
import website.jackl.generated.data.write
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.R

class AssignmentEditActivity : GradesActivity<ConstraintLayout>() {
    companion object {
        val ASSIGNMENT_EDIT_REQUEST = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_edit)

        parent = findViewById(R.id.parent)

        toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.edit_assignment)
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick)

        name = findViewById(R.id.editAssignment_name)
        points = findViewById(R.id.editAssignment_points)
        maxPoints = findViewById(R.id.editAssignment_maxPoints)
        category = findViewById(R.id.editAssignment_category)

        categories = intent.getStringArrayListExtra("categories")!!
        val categoriesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category.adapter = categoriesAdapter

        intent.getStringExtra("assignment")?.apply {
            val assignment = constructGradebookAssignment(JSONObject(this))!!

            name.text.apply {
                replace(0, length, assignment.name)
            }
            points.text.apply {
                replace(0, length, Gradebook.decimalFormat.format(assignment.points))
            }
            maxPoints.text.apply {
                replace(0, length, Gradebook.decimalFormat.format(assignment.maxPoints))
            }
            category.setSelection(categories.indexOf(assignment.category))

            this@AssignmentEditActivity.assignment = assignment
        }

        position = intent.getIntExtra("position", -1)

    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        val assignment = assignment
        when (item.itemId) {
            R.id.edit_assignment_delete -> {
                if (assignment == null) {
                    setResult(AppCompatActivity.RESULT_CANCELED)
                } else {
                    val intent = Intent()
                    intent.action = Intent.ACTION_DELETE
                    intent.putExtra("position", position)
                    setResult(AppCompatActivity.RESULT_OK, intent)
                }
            }

            R.id.edit_assignment_done -> {
                if (assignment == null) {
                    val intent = Intent()
                    intent.action = Intent.ACTION_INSERT
                    intent.putExtra("assignment", assignmentFromUi().write().toString())
                    setResult(AppCompatActivity.RESULT_OK, intent)
                } else {
                    val intent = Intent()
                    intent.action = Intent.ACTION_EDIT
                    intent.putExtra("assignment", assignmentFromUi().write().toString())
                    intent.putExtra("position", position as Int)

                    setResult(AppCompatActivity.RESULT_OK, intent)
                }
            }
        }
        finish()
        return true
    }

    private fun assignmentFromUi(): Gradebook.Assignment {
        var assignmentPoints: Double
        var assignmentMaxPoints: Double
        var assignmentName: String
        try {
            val parsedPoints = points.text.toString().toDouble()
            assignmentPoints = if (parsedPoints.isFinite()) parsedPoints else 0.0
        } catch (e: Throwable) {
            assignmentPoints = 0.0
        }
        try {
            val parsedMaxPoints = maxPoints.text.toString().toDouble()
            assignmentMaxPoints = if (parsedMaxPoints.isFinite()) parsedMaxPoints else 0.0
        } catch (e: Throwable) {
            assignmentMaxPoints = 0.0
        }

        assignmentName = name.text.toString()
        if (assignmentName.isBlank()) assignmentName = "Untitled Assignment"

        return Gradebook.Assignment(
                assignmentName,
                categories[category.selectedItemPosition],
                assignmentPoints,
                assignmentMaxPoints,
                true
        )
    }

    private lateinit var categories: List<String>
    private var assignment: Gradebook.Assignment? = null
    private var position: Int = -1

    private lateinit var toolbar: Toolbar
    private lateinit var name: EditText
    private lateinit var points: EditText
    private lateinit var maxPoints: EditText
    private lateinit var category: Spinner


}
