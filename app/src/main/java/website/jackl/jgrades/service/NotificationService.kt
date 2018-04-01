package website.jackl.jgrades.service

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.support.annotation.RequiresApi
import android.util.Log
import website.jackl.jgrades.Data.SessionData
import website.jackl.jgrades.Data.SettingsManager
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.Data.User
import website.jackl.jgrades.newStore
import website.jackl.jgrades.protocol.service.ServerService

@RequiresApi(21) class NotificationService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("Notification Job", "Start")
        store = newStore
        val global = store.loadGlobal()
        val email = global.activeEmail
        val password: String?
        val user: User?
        if (email != null) {
            user = store.loadUser(email)
            password = user.password
        } else {
            password = null
            user = null
        }

        val aeriesUrl = global.district?.url

        if (aeriesUrl != null && email != null && password != null && user?.preferredStudent != null) {
            student = user.preferredStudent
            val sessionData = SessionData(aeriesUrl = aeriesUrl, user = user)
            connection  = ServerService.Connection(this)
            connection.bindService(
                    sessionData,
                    {
                        this.service = it
                        it.addDesire(ServerService.Desire.AllGradebooks(
                                {
                                    Log.d("Notification Job", "End")
                                    connection.unbindService()
                                    jobFinished(params, false)},
                                {
                                    Log.d("Notification Job", "Error")
                                    connection.unbindService()
                                    jobFinished(params, true)}
                        ), false)
                    }
            )
        } else {
            jobFinished(params, false)
        }

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("Notification Job", "Stop")
        service?.removeDesire()
        if (this::connection.isInitialized) connection.unbindService()
        return true
    }

    private lateinit var student: Student.Info
    private lateinit var connection: ServerService.Connection
    private var service: ServerService.Connection.Binder? = null
    private lateinit var store: SettingsManager
}
