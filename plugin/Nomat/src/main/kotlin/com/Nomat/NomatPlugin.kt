package com.nomat

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class NomatPlugin : Plugin() {
    override fun load(context: Context) {
        NomatDomain.appContext = context.applicationContext
        openSettings = { ctx -> NomatDomain.openSettings(ctx) }
        registerMainAPI(Nomat())
        registerExtractorAPI(Hydrax())
		registerExtractorAPI(Dingtezuni())
        registerExtractorAPI(Movearnpre())
        registerExtractorAPI(Mivalyo())
        registerExtractorAPI(Bingezove())
        registerExtractorAPI(Ryderjet())
    }
}
