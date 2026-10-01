package com.cgvindo

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import com.lagradost.cloudstream3.app
import org.json.JSONObject

/**
 * Domain resolution for Cgvindo (id: cgvindo).
 * Order:
 *  1. User override from extension settings (SharedPreferences)
 *  2. Remote JSON at https://raw.githubusercontent.com/Young12-think/Cloudstream/main/sites-movie.json (in-memory cache, 6h TTL)
 *  3. Hardcoded fallback: https://cgvindo2.casa
 */
object CgvindoDomain {
    const val PROVIDER_ID = "cgvindo"
    const val REMOTE_JSON = "https://raw.githubusercontent.com/Young12-think/Cloudstream/main/sites-movie.json"
    const val FALLBACK = "https://cgvindo2.casa"

    private const val PREFS_NAME = "cloudstream_id_domains"
    private const val CACHE_TTL_MS = 6 * 60 * 60 * 1000L

    @Volatile var appContext: Context? = null

    @Volatile private var cachedDomains: List<String>? = null
    @Volatile private var cacheAt: Long = 0L

    private fun overrideDomain(): String? {
        val v = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString("domain_override_$PROVIDER_ID", null)?.trim()
        return v?.takeIf { it.isNotBlank() }?.removeSuffix("/")
    }

    fun setOverride(context: Context, domain: String?) {
        appContext = context.applicationContext
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("domain_override_$PROVIDER_ID", domain?.trim()?.ifBlank { null })
            .apply()
    }

    fun getOverride(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("domain_override_$PROVIDER_ID", null)?.trim()?.takeIf { it.isNotBlank() }

    /** Domains in priority order. Never empty: always ends with FALLBACK. */
    suspend fun resolveDomains(): List<String> {
        overrideDomain()?.let { return listOf(it) }
        val now = System.currentTimeMillis()
        var cached = cachedDomains
        if (cached == null || now - cacheAt > CACHE_TTL_MS) {
            val fresh = fetchRemoteDomains()
            if (fresh != null) {
                cached = fresh
                cachedDomains = fresh
                cacheAt = now
            }
        }
        return (cached ?: emptyList()) + FALLBACK
    }

    suspend fun resolveDomain(): String = resolveDomains().first()

    private suspend fun fetchRemoteDomains(): List<String>? {
        return try {
            val json = JSONObject(app.get(REMOTE_JSON).text)
            val sites = json.optJSONArray("sites") ?: return null
            for (i in 0 until sites.length()) {
                val s = sites.optJSONObject(i) ?: continue
                if (s.optString("id") != PROVIDER_ID) continue
                if (s.optString("status") == "dead") continue
                val arr = s.optJSONArray("domains") ?: continue
                val out = (0 until arr.length())
                    .map { arr.optString(it).trim().removeSuffix("/") }
                    .filter { it.isNotBlank() }
                return out.ifEmpty { null }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun openSettings(context: Context) {
        val input = EditText(context).apply {
            hint = "https://... (kosongkan = otomatis)"
            setText(getOverride(context) ?: "")
        }
        AlertDialog.Builder(context)
            .setTitle("Domain $PROVIDER_ID")
            .setMessage("Override manual domain. Kosongkan untuk kembali otomatis (JSON remote, lalu bawaan).")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ -> setOverride(context, input.text.toString()) }
            .setNeutralButton("Reset") { _, _ -> setOverride(context, null) }
            .setNegativeButton("Batal", null)
            .show()
    }
}
