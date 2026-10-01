package com.pencurimovie

import android.content.Context
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class PencurimoviePlugin: Plugin() {
    override fun load(context: Context) {
        PencurimovieDomain.appContext = context.applicationContext
        openSettings = { ctx -> PencurimovieDomain.openSettings(ctx) }
        registerMainAPI(Pencurimovie())
    }
}
