package info.aoisensi.donsiro

import android.app.Application
import android.preference.PreferenceManager

/**
 * Created by aoisensi on 2018/01/03.
 *
 */

class Donsiro : Application() {
    fun addAccount(accessToken: String, domain: String) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val accounts = pref.getStringSet("accounts", HashSet())
        //TODO accounts.add()
        pref.edit().putStringSet("accounts", accounts).apply()
    }
}