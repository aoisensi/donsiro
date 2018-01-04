package info.aoisensi.donsiro.api

import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by aoisensi on 2018/01/03.
 *
 */
interface MastodonService {

    @FormUrlEncoded
    @POST("/api/v1/apps")
    fun registerApp(
            @Field("client_name") clientName: String,
            @Field("redirect_uris") redirectUris: String = "urn:ietf:wg:oauth:2.0:oob",
            @Field("scopes") scopes: String = "read write follow"
    ): Call<MastodonApplication>

    companion object {
        private fun build(domain: String): MastodonService {
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://$domain")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            return retrofit.create(MastodonService::class.java)
        }

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
            return adapter.fromJson(response.body()?.string())
        }
    }
}