package website.jackl.jgrades.Data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by jack on 2/23/18.
 */
data class NotificationLocks(val summariesLock: Boolean = false, val gradebookLocks: List<String> = listOf()) {
}

fun writeNotificationLocks(notificationLocks: NotificationLocks): JSONObject {
    val json = JSONObject()
    notificationLocks.apply {
        json.put("summariesLock", summariesLock)

        val locks = JSONArray()
        gradebookLocks.forEach { locks.put(it) }

        json.put("gradebookLocks", locks)
    }
    return json
}

fun NotificationLocks.write(): JSONObject {
    return writeNotificationLocks(this)
}

fun constructNotificationLocks(jsonObject: JSONObject?): NotificationLocks? {
    if (jsonObject == null) return null

    val summariesLock = jsonObject.getBoolean("summariesLock")

    val locks = mutableListOf<String>()
    val jsonLocks = jsonObject.getJSONArray("gradebookLocks")

    var i = 0
    while (i < jsonLocks.length()) {
        locks.add(jsonLocks.getString(i))
        ++i
    }

    return NotificationLocks(summariesLock, locks)
}