package io.github.chitao1234.xdusrunlogin.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Credential(
    val id: String,
    val username: String,
    val password: String,
    val domain: String
)

object CredStore {
    private const val PREFS = "creds_prefs"
    private const val KEY_LIST = "creds_list_json"
    private const val KEY_DEFAULT_ID = "default_cred_id"
    private const val KEY_AUTO_LOGIN = "auto_login"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): List<Credential> {
        val raw = prefs(context).getString(KEY_LIST, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = ArrayList<Credential>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Credential(
                    id = o.optString("id"),
                    username = o.optString("username"),
                    password = o.optString("password"),
                    domain = o.optString("domain")
                )
            )
        }
        return list
    }

    fun saveList(context: Context, list: List<Credential>) {
        val arr = JSONArray()
        list.forEach {
            val o = JSONObject()
            o.put("id", it.id)
            o.put("username", it.username)
            o.put("password", it.password)
            o.put("domain", it.domain)
            arr.put(o)
        }
        prefs(context).edit { putString(KEY_LIST, arr.toString()) }
    }

    fun saveOrUpdate(context: Context, username: String, password: String, domain: String): Credential {
        val normUsername = username.trim()
        val normDomain = domain.trim()
        val list = load(context).toMutableList()
        val existing = list.firstOrNull { it.username == normUsername && it.domain == normDomain }
        val updated = if (existing != null) {
            val u = existing.copy(password = password)
            val idx = list.indexOf(existing)
            list[idx] = u
            u
        } else {
            val c = Credential(
                id = UUID.randomUUID().toString(),
                username = normUsername,
                password = password,
                domain = normDomain
            )
            list.add(c)
            c
        }
        saveList(context, list)
        return updated
    }

    fun setDefaultId(context: Context, id: String) {
        prefs(context).edit { putString(KEY_DEFAULT_ID, id) }
    }

    fun getDefaultId(context: Context): String? =
        prefs(context).getString(KEY_DEFAULT_ID, null)

    fun setAutoLogin(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_AUTO_LOGIN, enabled) }
    }

    fun getAutoLogin(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_LOGIN, false)

    fun removeById(context: Context, id: String) {
        val current = load(context)
        if (current.isEmpty()) return
        val filtered = current.filterNot { it.id == id }
        saveList(context, filtered)
        val def = getDefaultId(context)
        if (def == id) {
            prefs(context).edit { remove(KEY_DEFAULT_ID) }
        }
    }
}


