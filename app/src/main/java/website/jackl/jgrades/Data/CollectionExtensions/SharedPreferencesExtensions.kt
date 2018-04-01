package website.jackl.jgrades.Data.CollectionExtensions

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by jack on 1/4/18.
 */

fun JSONArray.jsonObjectIterator(): Iterator<JSONObject> {
    return object : Iterator<JSONObject> {
        override fun hasNext(): Boolean = index < this@jsonObjectIterator.length()
        override fun next(): JSONObject {
            return getJSONObject(index++)
        }

        var index = 0
    }
}

fun SharedPreferences.getJSONObject(key: String): JSONObject? {
    return JSONObject(getString(key, null) ?: return null)
}

fun SharedPreferences.Editor.putJSONObject(key: String, value: JSONObject) {
    putString(key, value.toString())
}