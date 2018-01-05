package info.aoisensi.donsiro.api

import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by aoisensi on 2018/01/03.
 *
 */
class MastodonService {
    companion object {
        fun registerApp(
                domain: String,
                clientName: String,
                redirectUris: String = "urn:ietf:wg:oauth:2.0:oob",
                scopes: String = "read write follow"
        ): MastodonApplication {
            val client = OkHttpClient()
            val request = Request.Builder().apply {
                url("https://$domain/api/v1/apps")
                post(FormBody.Builder().apply {
                    add("client_name", clientName)
                    add("redirect_uris", redirectUris)
                    add("scopes", scopes)
                }.build())
            }.build()
            val response = client.newCall(request).execute()
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter<MastodonApplication>(MastodonApplication::class.java)
            return adapter.fromJson(response.body()!!.string())!!
        }
    }
}