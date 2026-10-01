package com.indomax

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class IndomaxPlugin : Plugin() {
    override fun load(context: Context) {
        IndomaxDomain.appContext = context.applicationContext
        openSettings = { ctx -> IndomaxDomain.openSettings(ctx) }
        registerMainAPI(Indomax())

        registerExtractorAPI(ImaxStreams())
        registerExtractorAPI(ImaxStreamsCom())
    }
}
