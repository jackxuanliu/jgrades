package website.jackl.jgrades.activity

import android.app.Activity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.widget.Toolbar
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.R
import website.jackl.jgrades.protocol.request.aeries.GetStudentsRequest
import website.jackl.jgrades.recyclerAdapter.MyListAdapter
import website.jackl.jgrades.recyclerAdapter.StudentsAdapter
import website.jackl.jgrades.view.MyList

class SelectStudentActivity : GradesActivity<ConstraintLayout>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_student)

        parent = findViewById(R.id.parent)
        toolbar = findViewById(R.id.toolbar)

        list = findViewById(R.id.selectStudent_list)
        list.setAdapter(adapter)
        adapter.onItemClick = this::onStudentSelected
        list.onSrlPull = this::onServiceReady
    }

    override fun onStart() {
        super.onStart()
        list.startLoading()
    }

    override fun onServiceReady() {
        val studentsRequest = GetStudentsRequest(this::onStudentsGot, {
            list.stopLoading()
            this.onErrorSnackbar(it)
        })
        service.request(studentsRequest)
    }

    private fun onStudentsGot(students: List<Student.Info>, currentStudent: Student.Info) {
        adapter.items.clear()
        adapter.items.addAll(students)
        adapter.notifyDataSetChanged()
        list.stopLoading()
    }

    private fun onStudentSelected(position: Int, student: Student.Info) {
        val email = store.loadGlobal().activeEmail
        if (email == null) {
            finish()
        } else {
            val user = store.loadUser(email)
            store.saveUser(user.copy(preferredStudent = student))
            val manager = NotificationManagerCompat.from(this)
            manager.cancelAll()
            finish()
        }
    }

    private lateinit var toolbar: Toolbar
    private lateinit var list: MyList
    private val adapter: StudentsAdapter = StudentsAdapter()
}
