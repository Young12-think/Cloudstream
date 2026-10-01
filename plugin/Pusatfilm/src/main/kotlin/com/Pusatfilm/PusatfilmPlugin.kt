package com.pusatfilm

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class PusatfilmPlugin : Plugin() {

    override fun load(context: Context) {
        PusatfilmDomain.appContext = context.applicationContext
        openSettings = { ctx -> PusatfilmDomain.openSettings(ctx) }
        registerMainAPI(Pusatfilm())
        registerExtractorAPI(Kotakajaib())
    }
}
