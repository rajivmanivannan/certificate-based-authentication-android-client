package com.rm.cba.security

import android.content.Context
import android.security.KeyChain
import android.security.KeyChainException
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509KeyManager

/**
 * CustomX509KeyManager
 *<p>
 * determine the set of aliases that are available for negotiations based on the criteria presented,
 * select the best alias based on the criteria presented, and obtain the corresponding key material for given aliases.
 */
class CustomX509KeyManager(
    private val alias: String,
    private val certChain: Array<X509Certificate>,
    private val privateKey: PrivateKey
) : X509KeyManager {
    override fun chooseClientAlias(
        arg0: Array<String>,
        arg1: Array<Principal>,
        arg2: Socket
    ): String {
        return alias
    }

    override fun getCertificateChain(alias: String): Array<X509Certificate>? {
        return if (this.alias == alias) certChain else null
    }

    override fun getPrivateKey(alias: String): PrivateKey? {
        return if (this.alias == alias) privateKey else null
    }

    // Unused Methods for this implementation.
    override fun chooseServerAlias(
        keyType: String,
        issuers: Array<Principal>,
        socket: Socket
    ): String {
        throw UnsupportedOperationException()
    }

    override fun getClientAliases(
        keyType: String,
        issuers: Array<Principal>
    ): Array<String> {
        throw UnsupportedOperationException()
    }

    override fun getServerAliases(
        keyType: String,
        issuers: Array<Principal>
    ): Array<String> {
        throw UnsupportedOperationException()
    }

    companion object {
        @Throws(CertificateException::class)
        fun fromAlias(
            context: Context,
            alias: String
        ): CustomX509KeyManager {
            val certChain: Array<X509Certificate>?
            val privateKey: PrivateKey?
            try {
                certChain = KeyChain.getCertificateChain(context, alias)
                privateKey = KeyChain.getPrivateKey(context, alias)
            } catch (e: KeyChainException) {
                throw CertificateException(e)
            } catch (e: InterruptedException) {
                throw CertificateException(e)
            }
            if (certChain == null || privateKey == null) {
                throw CertificateException("Can't access the certificate from keystore")
            }
            return CustomX509KeyManager(alias, certChain, privateKey)
        }
    }
}