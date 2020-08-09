package com.rm.cba.helper


import android.content.Context
import android.security.KeyChain
import android.security.KeyChainException
import com.rm.cba.app.AppApplication
import com.rm.cba.utils.LoggerUtils
import java.security.PrivateKey
import java.security.cert.X509Certificate

class KeyChainHelper {

    companion object {

        private val TAG: String = KeyChainHelper::class.java.simpleName
        private val context: Context = AppApplication.applicationContext

        fun isKeyChainAccessible(alias: String): Boolean {
            return (KeyChain.getCertificateChain(context, alias) != null
                    && KeyChain.getPrivateKey(context, alias) != null)
        }

        fun getCertificateChain(
            alias: String
        ): Array<out X509Certificate>? {
            try {
                return KeyChain.getCertificateChain(context, alias)
            } catch (e: KeyChainException) {
                LoggerUtils.e(TAG, e)
            } catch (e: InterruptedException) {
                LoggerUtils.e(TAG, e)
            }
            return null
        }

        fun getPrivateKey(alias: String): PrivateKey? {
            try {
                return KeyChain.getPrivateKey(context, alias)
            } catch (e: KeyChainException) {
                LoggerUtils.e(TAG, e)
            } catch (e: InterruptedException) {
                LoggerUtils.e(TAG, e)
            }
            return null
        }


        fun getCertificateAndPrivateKeyInformation(alias: String): String {
            val certs: Array<out X509Certificate>? =
                getCertificateChain( alias)
            val privateKey = getPrivateKey(alias)
            val sb = StringBuffer()
            for (cert in certs!!) {
                sb.append(cert.issuerDN)
                sb.append("\n ---------------------------------------------------------------------- \n")
            }
            if (privateKey != null) {
                sb.append("Private key Algorithm: " + privateKey.algorithm)
            }
            return sb.toString()
        }
    }
}