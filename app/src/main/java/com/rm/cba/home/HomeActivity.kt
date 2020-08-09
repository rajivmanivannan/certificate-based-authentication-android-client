package com.rm.cba.home

import android.os.Bundle
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.rm.cba.BuildConfig
import com.rm.cba.R
import com.rm.cba.app.AppConstants.Companion.HOST_NAME
import com.rm.cba.app.AppPreference
import com.rm.cba.helper.KeyChainHelper
import com.rm.cba.networking.AppEndPoints
import com.rm.cba.networking.ServiceBuilder
import com.rm.cba.security.CustomX509KeyManager
import com.rm.cba.security.CustomX509TrustManager
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext

class HomeActivity : AppCompatActivity(), KeyChainAliasCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tb_home_toolbar.title = getString(R.string.app_title)
        setSupportActionBar(tb_home_toolbar)

        val progressBar = ProgressBar(this)
        //setting height and width of progressBar
        progressBar.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Add ProgressBar to our layout
        rl_home_loading?.addView(progressBar)

        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_choose -> {
                chooseCertificate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun init() {
        Thread(Runnable {
            val alias: String = AppPreference.certificateAlias
            //Verify the certificate is selected and accessible
            if (KeyChainHelper.isKeyChainAccessible(alias)) {
                val customX509TrustManager = getTrustManager() // CustomX509TrustManager
                val customX509KeyManager =
                    CustomX509KeyManager.fromAlias(this, alias) //CustomX509KeyManager
                val sslContext = getSSLContext(customX509TrustManager, customX509KeyManager)

                val client: OkHttpClient = OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory, customX509TrustManager)
                    .hostnameVerifier(HostnameVerifier { hostname, _ ->
                        (hostname == HOST_NAME) // Verify hostname of the Endpoint server
                    })
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level =
                            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                    })
                    .build()
                runOnUiThread {
                    getCurrentUserData(client)
                }
            } else {
                runOnUiThread {
                    val snackBar = Snackbar.make(
                        cl_home_container,
                        getString(R.string.home_select_your_client_certificate),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackBar.setAction(getString(R.string.home_ok)) {
                        snackBar.dismiss()
                        chooseCertificate()
                    }.show()
                }
            }
        }).start()
    }

    private fun chooseCertificate() {
        KeyChain.choosePrivateKeyAlias(
            this,// Activity
            this, // Callback response
            arrayOf(),  // Key types - The acceptable types of asymmetric keys such as "RSA", "EC" or null.
            null,  // Issuers
            "localhost",  // Host
            -1,  // Port
            ""// All Alias
        )
    }

    private fun getTrustManager(): CustomX509TrustManager {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = resources.openRawResource(R.raw.root_ca)
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }
        return CustomX509TrustManager(keyStore)
    }

    @Throws(
        CertificateException::class,
        KeyManagementException::class,
        KeyStoreException::class,
        NoSuchAlgorithmException::class
    )
    fun getSSLContext(
        trustManager: CustomX509TrustManager,
        keyManager: CustomX509KeyManager
    ): SSLContext {
        val sslContext: SSLContext
        sslContext = try {
            SSLContext.getInstance("TLSv1.2") //TlsVersion.TLS_1_2
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("No such algorithm found:", e)
        }
        sslContext.init(
            arrayOf(keyManager),
            arrayOf(trustManager),
            SecureRandom()
        )
        return sslContext
    }

    private fun getCurrentUserData(client: OkHttpClient) {
        val request = ServiceBuilder.buildService(client, AppEndPoints::class.java)
        val call = request.getCurrentUserData()
        rl_home_loading.visibility = View.VISIBLE
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                rl_home_loading.visibility = View.GONE
                if (response.isSuccessful) {
                    tv_home_response.text = response.body()?.charStream()?.readText()
                } else {
                    tv_home_response.text = response.errorBody()?.charStream()?.readText()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                rl_home_loading.visibility = View.GONE
                tv_home_response.text = t.message.toString()
            }
        })
    }

    override fun alias(alias: String?) {
        if (alias != null) {
            AppPreference.certificateAlias = alias
            init()
        }
    }
}