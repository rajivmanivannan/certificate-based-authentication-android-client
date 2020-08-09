package com.rm.cba.security

import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * CustomX509TrustManager
 *<p>
 * CustomX509TrustManager is X509TrustManager to trust the chain of certificate
 * provided by your server certificate authority.
 */
class CustomX509TrustManager(keystore: KeyStore?) :
    X509TrustManager {
    private val x509TrustManager: X509TrustManager
    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return x509TrustManager.acceptedIssuers
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
        x509TrustManager.checkClientTrusted(chain, authType)
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
        if (chain.size == 1) {
            chain[0].checkValidity()
        } else {
            x509TrustManager.checkServerTrusted(chain, authType)
        }
    }

    init {
        val factory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        factory.init(keystore)
        val trustManagers = factory.trustManagers
        if (trustManagers.isEmpty()) {
            throw NoSuchAlgorithmException("TrustManager not found")
        }
        x509TrustManager = trustManagers[0] as X509TrustManager
    }
}