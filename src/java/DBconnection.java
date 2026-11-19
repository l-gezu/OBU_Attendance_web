package obuattendance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.net.URI;
import java.net.URLDecoder;

class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String databaseUrl = System.getenv("DATABASE_URL");

            if (databaseUrl == null || databaseUrl.isEmpty()) {
                throw new SQLException("DATABASE_URL not configured");
            }

            URI uri = URI.create(databaseUrl);

            String userInfo = uri.getRawUserInfo();
            String[] credentials = userInfo.split(":", 2);

            String user = URLDecoder.decode(
                credentials[0].replace("+", "%2B"), "UTF-8"
            );

            String password = URLDecoder.decode(
                credentials[1].replace("+", "%2B"), "UTF-8"
            );

            String database = uri.getPath();

            String jdbcUrl = "jdbc:mysql://" + uri.getHost()
                    + ":" + uri.getPort()
                    + database
                    + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());

            return DriverManager.getConnection(
                jdbcUrl, user, password
            );

        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
            return null;
        }
    }
}