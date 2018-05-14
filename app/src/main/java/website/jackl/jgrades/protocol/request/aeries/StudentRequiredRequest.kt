package website.jackl.jgrades.protocol.request.aeries

import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.protocol.ServerError

/**
 * Created by jack on 1/20/18.
 */
// NOTE: preferredStudent must be set before performing request
abstract class StudentRequiredRequest(onError: (ServerError) -> Unit) : LoginRequiredRequest(onError) {
    var skipStudent = false

    data class Result<T>(val result: T, val student: Student.Info)

    lateinit var student: Student.Info

    open fun performPostStudent(service: ServiceInterface) {
        super.performPostLogin(service)
    }

    override final fun performPostLogin(service: ServiceInterface) {
        if (skipStudent) {
            performPostStudent(service)
        } else {
            this.service = service

            val getStudents = GetStudentsRequest(this::onGetStudentsResult, onError)
            getStudents.perform(service)
        }
    }

    private fun onGetStudentsResult(students: List<Student.Info>, currentStudent: Student.Info) {
        val service = service
        val preferredStudent = service!!.sessionData.user!!.preferredStudent

        if (preferredStudent == null) {
            student = currentStudent // if there is no preference just go with whatever it is
            performPostStudent(service)
        } else {
            if (currentStudent != preferredStudent) {
                if (students.contains(preferredStudent)) {
                    val changeStudent = ChangeStudentRequest(preferredStudent, this::onStudentChanged, onError)
                    changeStudent.perform(service)
                    student = preferredStudent
                } else {
                    student = currentStudent
                    performPostStudent(service)
                }
            } else {
                performPostStudent(service)
                student = preferredStudent
            }
        }


    }

    private fun onStudentChanged() {
        performPostStudent(service!!)
    }

    private var service: ServiceInterface? = null

}