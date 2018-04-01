package website.jackl.jgrades.protocol.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import website.jackl.generated.data.write
import website.jackl.jgrades.Data.Gradebook
import website.jackl.jgrades.Data.SessionData
import website.jackl.jgrades.Data.SettingsManager
import website.jackl.jgrades.Data.Student
import website.jackl.jgrades.R
import website.jackl.jgrades.activity.AssignmentsActivity
import website.jackl.jgrades.activity.CLASS_UPDATES_CHANNEL_ID
import website.jackl.jgrades.fragment.classUpdateInterval
import website.jackl.jgrades.fragment.classUpdateStatus
import website.jackl.jgrades.fragment.defaultPrefs
import website.jackl.jgrades.newStore
import website.jackl.jgrades.protocol.ServerError
import website.jackl.jgrades.protocol.request.ServerRequest
import website.jackl.jgrades.protocol.request.aeries.GradebookDetailsRequest
import website.jackl.jgrades.protocol.request.aeries.GradebookSummariesRequest
import website.jackl.jgrades.protocol.request.aeries.StudentRequiredRequest
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.CookieStore
import java.util.*
import android.app.PendingIntent
import android.content.res.AssetManager
import android.support.v4.app.TaskStackBuilder
import android.support.v4.view.LayoutInflaterCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewParent
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import org.apache.commons.io.IOUtils
import org.bouncycastle.openssl.PEMReader
import website.jackl.jgrades.activity.MainActivity
import website.jackl.jgrades.protocol.ExceptionTrustManager
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.spec.X509EncodedKeySpec
import javax.net.ssl.SSLContext


class ServerService : Service() {
    sealed class Desire(val onSatisfied: () -> Unit, val onError: (ServerError) -> Unit) {
        class Summary(onSatisfied: () -> Unit, onError: (ServerError) -> Unit): Desire(onSatisfied, onError)

        class SingleGradebook(val numberTerm: String, onSatisfied: () -> Unit, onError: (ServerError) -> Unit): Desire(onSatisfied, onError)

        class AllGradebooks(onSatisfied: () -> Unit, onError: (ServerError) -> Unit): Desire(onSatisfied, onError)
    }

    class Connection(val context: Context) : ServiceConnection {

        var sessionData: SessionData = SessionData()
        get() = field
        private set(value) {field = value}

        fun bindService(sessionData: SessionData, onConnected: (Binder) -> Unit) { // shortcut for binding service
            if (bound) unbindService()
            val serviceIntent = Intent(context, ServerService::class.java)
            context.bindService(serviceIntent, this, Service.BIND_AUTO_CREATE)
            bound = true
            this.callback = onConnected
            this.sessionData = sessionData
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.d("ServerService: ", "Died")
        }

        fun unbindService() {
            binder.cancelAll(uuid)
            context.unbindService(this)
            bound = false
        }

        private var bound = false

        fun changeSessionData(newSessionData: SessionData) {
            binder.changeSessionData(newSessionData)
            this.sessionData = newSessionData
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            this.binder = binder as ServerService.Binder

            changeSessionData(sessionData)

            callback(Binder())

        }

        inner class Binder {
            fun attachAd(parent: ViewGroup) {
                binder.attachAd(parent)
            }

            fun request(request: ServerRequest) {
                request.perform(serviceInterface)
            }

            fun addDesire(desire: Desire, start: Boolean = true) {
                binder.addDesire(this, uuid, desire, start)
            }

            fun removeDesire() {
                binder.removeDesire(this, uuid)
            }

            private val serviceInterface = object : ServerRequest.ServiceInterface {
                override fun addRequest(request: Request<out Any>) {
                    request.tag = uuid
                    binder.addRequest(request)
                }

                override fun clearCookies() {
                    binder.clearCookies()
                }

                override val sessionData: SessionData get() = binder.sessionData
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // TODO maybe reconnect here
        }

        private val uuid: UUID = UUID.randomUUID()

        lateinit private var binder: ServerService.Binder
        lateinit private var callback: (Binder) -> Unit
    }

    inner class Binder : android.os.Binder() {
        var sessionData: SessionData = SessionData()

        fun changeSessionData(newSessionData: SessionData) {
            if (newSessionData != sessionData) {
                cancelAll()
                if (updateSession != null) {
                    updateSession!!.stop()
                }
                if (newSessionData.user != sessionData.user) {
                    clearCookies()
                }
                sessionData = newSessionData
            }
        }

        fun addRequest(request: Request<out Any>) {
            requestQueue.add(request)
        }

        fun clearCookies() {
            cookieStore.removeAll()
        }

        fun cancelAll(tag: Any) {
            requestQueue.cancelAll(tag)
        }

        fun cancelAll() {
            requestQueue.cancelAll { true }
        }

        fun addDesire(binder: Connection.Binder, uuid: UUID, desire: Desire, start: Boolean) {
            val serviceInterface = object : ServerRequest.ServiceInterface {
                override fun addRequest(request: Request<out Any>) {
                    this@Binder.addRequest(request)
                }

                override fun clearCookies() {
                    this@Binder.clearCookies()
                }

                override val sessionData: SessionData = this@Binder.sessionData
            }


            desires.put(uuid, desire)

            if (updateSession == null) {
                updateSession = UpdateSession(serviceInterface)
                updateSession!!.start()

                if (start) {
                    val intent = Intent(this@ServerService, ServerService::class.java)
                    startService(intent)
                }
            }
        }

        fun removeDesire(binder: Connection.Binder, uuid: UUID) {
            desires.remove(uuid)
        }

        fun attachAd(parent: ViewGroup /* TODO compat or not? */) {
            val ad = ad
            if (ad == null) {
                val newAd = LayoutInflater.from(this@ServerService).inflate(R.layout.view_ad, parent, false) as AdView
                parent.addView(newAd)
                newAd.loadAd(AdRequest.Builder().build())
                this@ServerService.ad = newAd
            } else {
                (ad.parent as ViewGroup).removeView(ad)
                parent.addView(ad)
            }
        }

    }


    override fun onCreate() {
        super.onCreate()

        store = newStore
        cookieStore = PersistentCookieStore(this)

        CookieHandler.setDefault(CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL))

        val trustManager = ExceptionTrustManager(loadCertificateExceptions())

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)

        val cache =  DiskBasedCache(cacheDir, 1024 * 1024)
        val network = BasicNetwork(HurlStack(null, sslContext.socketFactory))

        requestQueue = RequestQueue(cache, network)

        requestQueue.start()

    }

    override fun onDestroy() {
        super.onDestroy()

        requestQueue.stop()

    }

    override fun onBind(intent: Intent): IBinder? {
        return Binder()
    }

    lateinit private var requestQueue: RequestQueue
    lateinit private var cookieStore: CookieStore

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return START_NOT_STICKY
    }

    inner class UpdateSession(val serviceInterface: ServerRequest.ServiceInterface) {


        fun start() {
            val summariesRequest = GradebookSummariesRequest(this::onSummariesResult, this::sendError)
            summariesRequest.perform(serviceInterface)
        }

        fun stop() {
            for (entry in desires) {
                val desire = entry.value
                desire.onError(ServerError.UNKNOWN) // error to all uncompleted desires
            }
            desires.clear()
            stopSelf()
            updateSession = null
        }

        private fun sendError(error: ServerError) {
            for (entry in desires) {
                val desire = entry.value
                desire.onError(error)
            }
            desires.clear()

            stop()
        }

        private fun onSummariesResult(result: StudentRequiredRequest.Result<List<Gradebook.Summary>>) {
            summaryResultTime = System.currentTimeMillis()

            store.saveGradebookSummaries(result.student, result.result)
            val student = store.loadStudent(result.student)
            store.saveStudent(student.copy()) // TODO I don't know what this does

            val allGradebooks = mutableListOf<String>()
            for (gradebook in result.result) {
                allGradebooks.add(gradebook.numberTerm)
            }

            remainingGradebooks = allGradebooks

            val remainingIt = remainingGradebooks.listIterator()
            while (remainingIt.hasNext()) {
                val numberTerm = remainingIt.next()
                val gradebook = store.loadGradebook(result.student, numberTerm)
                gradebook?.apply {
                    if (lastDetailsCheck > summary.lastUpdated) {
                        remainingIt.remove()
                        updatedGradebooks.add(numberTerm)
                    }
                }
            }

            val desiresIt = desires.iterator()

            while (desiresIt.hasNext()) {
                val entry = desiresIt.next()
                val desire = entry.value
                when (desire) {
                    is Desire.SingleGradebook -> {
                        if (updatedGradebooks.contains(desire.numberTerm)) {
                            desire.onSatisfied()
                            desiresIt.remove()
                        } else {
                            if (!remainingGradebooks.contains(desire.numberTerm)) { // if Aeries does not host the desired gradebook
                                desire.onError(ServerError.UNKNOWN)
                                desiresIt.remove()
                            }
                        }
                    }
                    is Desire.Summary -> {
                        desire.onSatisfied()
                        desiresIt.remove()
                    }
                    is Desire.AllGradebooks -> {
                        if (remainingGradebooks.isEmpty()) {
                            desire.onSatisfied()
                            desiresIt.remove()
                        }
                    }
                }
            }

            updateNextGradebook()

        }

        private fun updateNextGradebook() {
            if (remainingGradebooks.isEmpty()) {
                stop()
                return
            }

            val priority = priority
            val toUpdate: String

            if (priority == null) {
                toUpdate = remainingGradebooks.last()
            } else {
                if (remainingGradebooks.contains(priority)) {
                    toUpdate = priority
                } else {
                    toUpdate = remainingGradebooks.last()
                }
            }

            // TODO debug update only new ones

            val detailsRequest = GradebookDetailsRequest(toUpdate, {
                val gradebook = store.loadGradebook(it.student, toUpdate)!! // should not be null because they just clicked on the class
                val newGradebook = gradebook.copy(details = it.result, lastDetailsCheck = summaryResultTime)

                if (gradebook.details != null) {
                    if ((!isLocked(newGradebook.summary.numberTerm)) && isChanged(gradebook.details.detailedSummaryData, it.result.detailedSummaryData)) {
                        sendNotification(it.student, newGradebook.summary)
                    }
                }

                store.saveGradebook(it.student, newGradebook)
                remainingGradebooks.remove(toUpdate)
                updatedGradebooks.add(toUpdate)


                val desiresIt = desires.iterator()

                while (desiresIt.hasNext()) {
                    val entry = desiresIt.next()
                    val desire = entry.value

                    when (desire) {
                        is Desire.Summary ->{
                            desire.onSatisfied()
                            desiresIt.remove()
                        }
                        is Desire.SingleGradebook -> {
                            if (updatedGradebooks.contains(desire.numberTerm)) {
                                desire.onSatisfied()
                                desiresIt.remove()
                            } else if (!remainingGradebooks.contains(desire.numberTerm)) {
                                desire.onError(ServerError.UNKNOWN)
                                desiresIt.remove()
                            } else {
                                this.priority = desire.numberTerm
                            }
                        }
                        is Desire.AllGradebooks -> {
                            if (remainingGradebooks.isEmpty()) {
                                desire.onSatisfied()
                                desiresIt.remove()
                            }
                        }
                    }
                }

                updateNextGradebook()
            }, this::sendError)

            detailsRequest.perform(serviceInterface)
        }

        private fun sendNotification(student: Student.Info, summary: Gradebook.Summary) {
            val prefs = defaultPrefs

            if (prefs.classUpdateStatus == false || prefs.classUpdateInterval <= 0) return

            val intent = Intent(this@ServerService, AssignmentsActivity::class.java)
            intent.putExtra("student", student.write().toString())
            intent.putExtra("numberTerm", summary.numberTerm)

            val pendingIntent = TaskStackBuilder.create(this@ServerService)
                    // add all of DetailsActivity's parents to the stack,
                    // followed by DetailsActivity itself
                    .addNextIntentWithParentStack(intent)
                    .getPendingIntent(summary.numberTerm.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT)


            val builder = NotificationCompat.Builder(this@ServerService, CLASS_UPDATES_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_icon)
                    .setContentTitle(summary.name)
                    .setContentText("This class was recently updated.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)


            val manager = NotificationManagerCompat.from(this@ServerService)
            manager.notify(summary.numberTerm.hashCode(), builder.build())

        }

        private fun isLocked(numberTerm: String): Boolean {
            val notification = store.loadNotificationLocks()
            if (notification.summariesLock) return true

            for (numberTermLock in notification.gradebookLocks) {
                if (numberTermLock == numberTerm) return true
            }

            return false
        }

        private fun isChanged(old: Gradebook.DetailedSummaryData, new: Gradebook.DetailedSummaryData): Boolean {
            val oldCategories = old.categories
            val newCategories = new.categories

            if (oldCategories.size != newCategories.size) return true

            for (newEntry in newCategories) {
                val newKey = newEntry.key

                val newValue = newEntry.value
                val oldValue = oldCategories[newKey]

                if (oldValue == null) return true

                if (oldValue.points != newValue.points) return true
                if (oldValue.maxPoints != newValue.maxPoints) return true
            }

            return false
        }

        private var priority: String? = null

        lateinit var remainingGradebooks: MutableList<String>
        val updatedGradebooks: MutableList<String> = mutableListOf()
    }

    var summaryResultTime: Long = 0
    lateinit var store: SettingsManager
    private var ad: AdView? = null

    val desires: MutableMap<UUID, Desire> = mutableMapOf()
    private var updateSession: UpdateSession? = null
}

fun Context.loadCertificateExceptions(): List<Certificate> {
    val exceptions: MutableList<Certificate> = mutableListOf()

    val path = "certs"
    val certs = assets.list(path)
    for (cert in certs) {
        Log.d("cert", cert)
        val input = assets.open(path + "/" + cert)
        val reader = PEMReader(InputStreamReader(input))


       exceptions.add(reader.readObject() as Certificate)
    }

    return exceptions
}