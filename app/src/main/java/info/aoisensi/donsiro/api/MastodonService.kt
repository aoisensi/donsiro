package info.aoisensi.donsiro.api

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
    @POST("v1/apps")
    fun registerApp(
            @Field("client_name") clientName: String,
            @Field("redirect_uris") redirectUris: String = "urn:ietf:wg:oauth:2.0:oob",
            @Field("scopes") scopes: String = "read write follow"
    ): Call<MastodonApplication>

    companion object {
        fun build(domain: String): MastodonService {
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://$domain/api/")
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            return retrofit.create(MastodonService::class.java)
        }
    }
}