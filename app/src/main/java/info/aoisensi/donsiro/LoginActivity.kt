package info.aoisensi.donsiro

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.LoaderManager.LoaderCallbacks
import android.content.AsyncTaskLoader
import android.content.Intent
import android.content.Loader
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Scope
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration
import com.sys1yagi.mastodon4j.api.method.Apps
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.OkHttpClient

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Any> {

    var mAppRegistration: AppRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sign_in_button.setOnClickListener { attemptLogin() }

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {

        // Reset errors.
        domain.error = null

        // Store values at the time of the login attempt.
        val domainStr = domain.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid domain.
        if (TextUtils.isEmpty(domainStr)) {
            domain.error = getString(R.string.error_field_required)
            focusView = domain
            cancel = true
        } else if (!isDomainValid(domainStr)) {
            domain.error = getString(R.string.error_invalid_email)
            focusView = domain
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showProgress(true)
            val bundle = Bundle()
            bundle.putString("domain", domainStr)
            loaderManager.initLoader(1, bundle, this)
        }
    }

    private fun attemptAuth(code: String) {
        showProgress(true)
        val bundle = Bundle()
        bundle.putString("domain", domain.text.toString())
        bundle.putString("code", code)
        loaderManager.initLoader(2, bundle, this)
    }

    private fun isDomainValid(domain: String): Boolean {
        return domain.contains(".")
    }

    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })

    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Any>? {
        return when (i) {
            1 -> LoginTaskLoader(this, bundle!!.getString("domain"))
            2 -> AuthTaskLoader(this, bundle!!.getString("domain"), mAppRegistration!!, bundle.getString("code"))
            else -> throw Exception()
        }
    }

    override fun onLoadFinished(loader: Loader<Any>, data: Any) {
        val loaderId = loader.id
        loaderManager.destroyLoader(loaderId)
        when (loaderId) {
            1 -> {
                data as Pair<*, *>
                mAppRegistration = data.first as AppRegistration
                showProgress(false)
                // Show browser
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.second as String)))
            }
            2 -> {
                showProgress(false)
                //TODO
                Toast.makeText(this, data as String, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Any>) {

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data ?: return
        mAppRegistration ?: return
        val code = intent.data.getQueryParameter("code")
        attemptAuth(code)
    }

    class LoginTaskLoader(context: LoginActivity, private val mDomain: String) : AsyncTaskLoader<Any>(context) {
        override fun loadInBackground(): Any {
            val apps = Apps(MastodonClient.Builder(mDomain, OkHttpClient.Builder(), Gson()).build())
            val scope = Scope(Scope.Name.ALL)
            val redirectUris = "donsiro://oauth2"
            val app = apps.createApp(
                    clientName = "Donsiro",
                    redirectUris = redirectUris,
                    scope = scope
            ).execute()
            val url = apps.getOAuthUrl(
                    clientId = app.clientId,
                    redirectUri = redirectUris,
                    scope = scope
            )
            return Pair(app, url)
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }

    class AuthTaskLoader(context: LoginActivity,
                         private val mDomain: String,
                         private val mAppRegistration: AppRegistration,
                         private val mCode: String
    ) : AsyncTaskLoader<Any>(context) {
        override fun loadInBackground(): Any {
            val apps = Apps(MastodonClient.Builder(mDomain, OkHttpClient.Builder(), Gson()).build())
            return apps.getAccessToken(
                    mAppRegistration.clientId,
                    mAppRegistration.clientSecret,
                    mAppRegistration.redirectUri,
                    mCode,
                    "authorization_code"
            ).execute().accessToken
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }
}
