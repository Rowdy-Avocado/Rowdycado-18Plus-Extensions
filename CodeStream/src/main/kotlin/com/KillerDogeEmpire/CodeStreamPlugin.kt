
package com.KillerDogeEmpire

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context
import com.KillerDogeEmpire.Alions
import com.KillerDogeEmpire.Animefever
import com.KillerDogeEmpire.Comedyshow
import com.KillerDogeEmpire.Embedrise
import com.KillerDogeEmpire.Embedwish
import com.KillerDogeEmpire.FilelionsTo
import com.KillerDogeEmpire.FilemoonNl
import com.KillerDogeEmpire.Flaswish
import com.KillerDogeEmpire.Gdmirrorbot
import com.KillerDogeEmpire.M4ufree
import com.KillerDogeEmpire.Multimovies
import com.KillerDogeEmpire.MultimoviesSB
import com.KillerDogeEmpire.Mwish
import com.KillerDogeEmpire.Netembed
import com.KillerDogeEmpire.Playm4u
import com.KillerDogeEmpire.Ridoo
import com.KillerDogeEmpire.CodeStream
import com.KillerDogeEmpire.Streamruby
import com.KillerDogeEmpire.Streamvid
import com.KillerDogeEmpire.Streamwish
import com.KillerDogeEmpire.TravelR
import com.KillerDogeEmpire.Uploadever
import com.KillerDogeEmpire.UqloadsXyz
import com.KillerDogeEmpire.VCloud
import com.KillerDogeEmpire.Yipsu

@CloudstreamPlugin
class CodeStreamPlugin: Plugin() {
    override fun load(context: Context) {
        // All providers should be added in this manner. Please don't edit the providers list directly.
        registerMainAPI(CodeStream())
        registerMainAPI(CodeStreamLite())
        registerExtractorAPI(Animefever())
        registerExtractorAPI(Multimovies())
        registerExtractorAPI(MultimoviesSB())
        registerExtractorAPI(Yipsu())
        registerExtractorAPI(Mwish())
        registerExtractorAPI(TravelR())
        registerExtractorAPI(Playm4u())
        registerExtractorAPI(VCloud())

        registerExtractorAPI(M4ufree())
        registerExtractorAPI(Streamruby())
        registerExtractorAPI(Streamwish())
        registerExtractorAPI(FilelionsTo())
        registerExtractorAPI(Embedwish())
        registerExtractorAPI(UqloadsXyz())
        registerExtractorAPI(Uploadever())
        registerExtractorAPI(Netembed())
        registerExtractorAPI(Flaswish())
        registerExtractorAPI(Comedyshow())
        registerExtractorAPI(Ridoo())
        registerExtractorAPI(Streamvid())
        registerExtractorAPI(Embedrise())
        registerExtractorAPI(Gdmirrorbot())
        registerExtractorAPI(FilemoonNl())
        registerExtractorAPI(Alions())
    }
}