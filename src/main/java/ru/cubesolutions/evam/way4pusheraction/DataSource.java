package ru.cubesolutions.evam.way4pusheraction;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Garya on 02.02.2018.
 */
public class DataSource {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        config.setDriverClassName(Config.JDBC_DRIVER);
        config.setJdbcUrl(Config.JDBC_URL);
        config.setUsername(Config.JDBC_USER);
        config.setPassword(Config.JDBC_PASSWORD);
        config.setMaximumPoolSize(1);
        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    private DataSource() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
