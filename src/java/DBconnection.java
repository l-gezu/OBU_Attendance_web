package obuattendance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

 class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/OBU_Attendance";

    private static final String USER = "root";

    private static final String PASSWORD = "lemgez12@";

    public static Connection getConnection() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

            return con;

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
            return null;
        }
    }
}