package net.opentsdb.utils.ssl;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Keqing Zhang
 * Date: 2019-12-11 17:08
 */
public class MysqlSslUtils {
    public static Logger log = LoggerFactory.getLogger(MysqlSslUtils.class);

    public static final String MYSQL_URL = "mysql_url";
    public static final String MYSQL_USER = "mysql_username";
    public static final String MYSQL_PASSWORD = "mysql_password";
    public static final String MYSQL_CLUSTER = "mysql_cluster";
    public static final String MYSQL_SSL_ENABLED = "mysql.ssl.enabled";
    public static final String MYSQL_SSL_KEYSTORE = "mysql.ssl.keyStore";
    public static final String MYSQL_SSL_KEYSTORE_PASSWORD = "mysql.ssl.keyStorePassword";
    public static final String MYSQL_SSL_TRUSTSTORE = "mysql.ssl.trustStore";
    public static final String MYSQL_SSL_TRUSTSTORE_PASSWORD = "mysql.ssl.trustStorePassword";

    /**
     * MySQL SSL配置
     * @param config
     * @throws AesException
     */
    public static void mysqlSslInit(net.opentsdb.utils.Config config) throws AesException {
        try {
            boolean enabled = config.hasProperty(MYSQL_SSL_ENABLED) ? config.getBoolean(MYSQL_SSL_ENABLED) : false;
            if (enabled) {
                String keyStore = config.getString(MYSQL_SSL_KEYSTORE);
                String keyStorePassword = config.getString(MYSQL_SSL_KEYSTORE_PASSWORD);
                String trustStore = config.getString(MYSQL_SSL_TRUSTSTORE);
                String trustStorePassword = config.getString(MYSQL_SSL_TRUSTSTORE_PASSWORD);
                initSSL(keyStore, keyStorePassword, trustStore, trustStorePassword);
            }
        } catch (NullPointerException npe) {
            // do nothing
            log.error("NPE in mysqlSslInit");
        }
    }

    private static void initSSL(String keyStore, String keyStorePassword, String trustStore, String trustStorePassword) throws AesException {
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", AesCrypt.getInstance().decrypt(keyStorePassword));
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", AesCrypt.getInstance().decrypt(trustStorePassword));
    }
}

