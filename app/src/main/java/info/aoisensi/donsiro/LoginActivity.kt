package info.aoisensi.donsiro

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.AsyncTaskLoader
import android.content.Loader
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import info.aoisensi.donsiro.api.MastodonApplication
import info.aoisensi.donsiro.api.MastodonService
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<MastodonApplication?> {

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
            loaderManager.initLoader(0, bundle, this)
        }
    }

    private fun isDomainValid(domain: String): Boolean {
        return domain.contains(".")
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
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

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<MastodonApplication?>? {
        return LoginTaskLoader(this, bundle!!.getString("domain"))
    }

    override fun onLoadFinished(loader: Loader<MastodonApplication?>?, data: MastodonApplication?) {
        if (data != null) {
            //TODO open web browser
            Toast.makeText(this, data.clientId, Toast.LENGTH_SHORT).show()
        }
        showProgress(false)
    }

    override fun onLoaderReset(loader: Loader<MastodonApplication?>?) {

    }

    class LoginTaskLoader(context: LoginActivity, private val mDomain: String) : AsyncTaskLoader<MastodonApplication?>(context) {
        override fun loadInBackground(): MastodonApplication? {
            val service = MastodonService.build(mDomain)
            val result = service.registerApp("Donsiro").execute()
            Log.d("donsiro", result.code().toString())
            return result.body()
            /*
            return try {
                service.registerApp("Donsiro").execute().body()
            } catch (e: Exception) {
                Log.w("donsiro", e.localizedMessage)
                null
            }
            */
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }
}
