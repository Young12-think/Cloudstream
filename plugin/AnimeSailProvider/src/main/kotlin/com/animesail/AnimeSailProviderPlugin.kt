package com.animesail

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class AnimeSailProviderPlugin: Plugin() {
    override fun load(context: Context) {
        AnimeSailProviderDomain.appContext = context.applicationContext
        openSettings = { ctx -> AnimeSailProviderDomain.openSettings(ctx) }
        registerMainAPI(AnimeSailProvider())
    }
}