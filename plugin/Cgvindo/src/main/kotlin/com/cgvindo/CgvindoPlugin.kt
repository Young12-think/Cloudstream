package com.cgvindo

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class CgvindoPlugin : Plugin() {
    override fun load(context: Context) {
        CgvindoDomain.appContext = context.applicationContext
        openSettings = { ctx -> CgvindoDomain.openSettings(ctx) }
        registerMainAPI(CgvindoProvider())
    }
}
