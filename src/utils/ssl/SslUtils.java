package net.opentsdb.utils.ssl;

import net.opentsdb.utils.ssl.AesCrypt;

import java.util.*;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.*;
import java.io.FileInputStream;

public class SslUtils {
    public static SSLContext getSSLContext(String keyStorePath, String keyPassword, String keyStorePassword, String trustKeyStorePath, String trustKeyStorePassword){
        try {
            if (keyPassword != null) {
                keyPassword = AesCrypt.getInstance().decrypt(keyPassword);
            }
            if (keyStorePassword != null) {
                keyStorePassword = AesCrypt.getInstance().decrypt(keyStorePassword);
            }
            KeyManagerFactory keyManagerFactory = getKeyManagerFactory(keyStorePath, keyStorePassword, keyPassword);

            if (trustKeyStorePassword != null) {
                trustKeyStorePassword = AesCrypt.getInstance().decrypt(trustKeyStorePassword);
            }
            TrustManagerFactory trustManagerFactory = getTrustManagerFactory(trustKeyStorePath, trustKeyStorePassword);

            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null, null);
            return sslContext;
        } catch (Exception e) {
            // logger.error("get ssl context fail", e);
            return null;
        }
    }

    public static KeyManagerFactory getKeyManagerFactory(String keyStorePath, String keyStorePassword, String keyPassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPassword.toCharArray());
        return keyManagerFactory;
    }

    public static TrustManagerFactory getTrustManagerFactory(String trustKeyStorePath, String trustKeyStorePassword) throws Exception {
        KeyStore trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustKeyStore.load(new FileInputStream(trustKeyStorePath), trustKeyStorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustKeyStore);
        return trustManagerFactory;
    }
}
