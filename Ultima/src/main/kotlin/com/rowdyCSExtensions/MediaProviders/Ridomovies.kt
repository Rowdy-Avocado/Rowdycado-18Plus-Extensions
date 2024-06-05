package com.KillerDogeEmpire

import com.KillerDogeEmpire.UltimaUtils.Category
import com.KillerDogeEmpire.UltimaUtils.LinkData
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.utils.ExtractorLink

class RidomoviesMediaProvider : MediaProvider() {
    override val name = "Ridomovies"
    override val domain = "https://ridomovies.tv"
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
