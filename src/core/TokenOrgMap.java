package net.opentsdb.core;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import net.opentsdb.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TokenOrgMap {
    private static final Logger LOG = LoggerFactory.getLogger(TokenOrgMap.class);
    private final MysqlConnectionPoolDataSource connectionPoolDataSource;
    private final Map<String, String> tokenToOrg = new HashMap<String, String>();

    public TokenOrgMap(Config config) throws SQLException{
        final String dbUrl = config.getString(Config.PROP_MYSQL_URL);
        final String dbUser = config.getString(Config.PROP_MYSQL_USER);
        final String dbPassword = config.getString(Config.PROP_MYSQL_PASSWORD);

        this.connectionPoolDataSource = new MysqlConnectionPoolDataSource();
        this.connectionPoolDataSource.setUrl(dbUrl);
        this.connectionPoolDataSource.setUser(dbUser);
        this.connectionPoolDataSource.setPassword(dbPassword);
        LOG.info("initialized db connection {}@{}", dbUser, dbUrl);
        load();
    }

    public Optional<String> getOrgNameForToken(String token) {
        if (Strings.isNullOrEmpty(token)) {
            return Optional.absent();
        }
        String orgName = this.tokenToOrg.get(token);
        return Optional.fromNullable(orgName);
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
            stmt = con.prepareStatement("select a.key, o.name from org as o join api_key as a on o.id = a.org_id;");
            rs = stmt.executeQuery();
            while(rs.next()){
                final String token = rs.getString("key");
                final String orgName = rs.getString("name");
                this.tokenToOrg.put(token, orgName);
            }
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
