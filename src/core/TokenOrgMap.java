package net.opentsdb.core;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import net.opentsdb.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.binary.Base64;

public class TokenOrgMap {
    private static final Logger LOG = LoggerFactory.getLogger(TokenOrgMap.class);
    private final MysqlConnectionPoolDataSource connectionPoolDataSource;
    private final Map<String, Prefix> tokenToOrg = new ConcurrentHashMap<String, Prefix>();

    public TokenOrgMap(Config config) throws SQLException{
        final String dbUrl = config.getString(Config.PROP_MYSQL_URL);
        final String dbUser = config.getString(Config.PROP_MYSQL_USER);
        String dbPassword = config.getString(Config.PROP_MYSQL_PASSWORD);
        try{
            byte[] decodeBase64 = Base64.decodeBase64(dbPassword.getBytes("UTF-8"));
            dbPassword =  new String(decodeBase64);
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        this.connectionPoolDataSource = new MysqlConnectionPoolDataSource();
        this.connectionPoolDataSource.setUrl(dbUrl);
        this.connectionPoolDataSource.setUser(dbUser);
        this.connectionPoolDataSource.setPassword(dbPassword);
        LOG.info("initialized db connection {}@{}", dbUser, dbUrl);
        final Timer timer = new Timer("TimerThread-refresh-orgtoken");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    load();
                } catch (Throwable t) {
                    LOG.error("failed to load token-org mapping", t);
                }
            }
        }, 0, 30000);
    }

    public Optional<Prefix> getPrefixForToken(String token) {
        if (Strings.isNullOrEmpty(token)) {
            return Optional.absent();
        }
        Prefix prefix = this.tokenToOrg.get(token);
        return Optional.fromNullable(prefix);
    }

    private void load() throws SQLException {
        LOG.info("loading TokenOrgMap");
        PooledConnection pcon = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            pcon = this.connectionPoolDataSource.getPooledConnection();
            con = pcon.getConnection();
            stmt = con.prepareStatement("select a.key, o.id, a.name from org as o join api_key as a on o.id = a.org_id;");
            rs = stmt.executeQuery();
            this.tokenToOrg.clear();
            int count = 0;
            while(rs.next()){
                final String token = rs.getString("key");
                final Long orgId = rs.getLong("id");
                final String systemId = rs.getString("name");
                this.tokenToOrg.put(token, new Prefix(orgId, systemId));
                count++;
            }
            LOG.info("total {} token-org mappings are loaded", count);
        } finally{
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
                if (pcon != null) pcon.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
