package info.aoisensi.donsiro.api

import com.squareup.moshi.Json

/**
 * Created by aoisensi on 2018/01/03.
 *
 */

data class MastodonApplication(
        @Json(name = "id") val id: String,
        @Json(name = "redirect_uri") val redirectUri: String,
        @Json(name = "client_id") val clientId: String,
        @Json(name = "client_secret") val clientSecret: String
)