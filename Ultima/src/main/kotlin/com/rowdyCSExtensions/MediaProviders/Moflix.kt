package com.KillerDogeEmpire

import com.KillerDogeEmpire.UltimaUtils.Category
import com.KillerDogeEmpire.UltimaUtils.LinkData
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.utils.ExtractorLink

class MoflixMediaProvider : MediaProvider() {
    override val name = "Moflix"
    override val domain = "https://moflix-stream.xyz"
    override val categories = listOf(Category.ANIME, Category.MEDIA)

    override suspend fun loadContent(
            url: String,
            data: LinkData,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
    ) {}

    // #region - Encryption and Decryption handlers
    // #endregion - Encryption and Decryption handlers

    // #region - Data classes
    // #endregion - Data classes
}
