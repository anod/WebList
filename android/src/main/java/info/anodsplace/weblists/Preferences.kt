package info.anodsplace.weblists

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import info.anodsplace.weblists.common.AppPreferences

class Preferences(private val sharedPreferences: SharedPreferences) : AppPreferences {

    constructor(context: Context): this(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))

    override var lastSiteId: Long
        get() = sharedPreferences.getLong("last-site-id", -1)
        set(value) { sharedPreferences.edit { putLong("last-site-id", value) } }
}