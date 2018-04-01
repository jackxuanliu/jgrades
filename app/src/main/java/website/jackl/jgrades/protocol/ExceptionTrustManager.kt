package website.jackl.jgrades.protocol

import android.util.Log
import java.security.KeyStore
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by jack on 3/3/18.
 */
class ExceptionTrustManager(val exceptions: List<Certificate>) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        defaultTrustManagers.forEach { it.checkClientTrusted(chain, authType) }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        try {
            defaultTrustManagers.forEach { it.checkServerTrusted(chain, authType) }
        } catch (e: Throwable) {
            if (chain == null) {
                throw e
            } else {
                val first = chain[0]
                for (cert in exceptions) {
                    if (first.encoded.contentEquals(cert.encoded)) {
                        return
                    }
                }
                throw e
            }

        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val acceptedIssuers = mutableListOf<X509Certificate>()

        for (trustManager in defaultTrustManagers) {
            acceptedIssuers.addAll(trustManager.acceptedIssuers)
        }

        return acceptedIssuers.toTypedArray()
    }

    private val defaultTrustManagers: List<X509TrustManager>

    init {
        val defaultTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        defaultTrustManagerFactory.init(null as KeyStore?)
        val defaultTrustManagers = mutableListOf<X509TrustManager>()

        for (trustManager in defaultTrustManagerFactory.trustManagers) {
            if (trustManager is X509TrustManager) {
                defaultTrustManagers.add(trustManager)
            }
        }

        this.defaultTrustManagers = defaultTrustManagers
    }
}