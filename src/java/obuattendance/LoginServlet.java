
package obuattendance;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String role = request.getParameter("role");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validate input
        if (role == null || username == null || password == null
                || role.trim().isEmpty()
                || username.trim().isEmpty()
                || password.isEmpty()) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html?error=1");
            return;
        }

        role = role.trim();
        username = username.trim();

        String table;
        String usernameColumn;

        // Check user role
        if ("admin".equals(role)) {

            table = "admins";
            usernameColumn = "username";

        } else if ("lecturer".equals(role)) {

            table = "lecturers";
            usernameColumn = "email";

        } else {

            response.sendRedirect(
                    request.getContextPath() + "/login.html?error=1");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException(
                        "Database connection failed");
            }

            String sql = "SELECT * FROM " + table
                    + " WHERE " + usernameColumn + " = ?";

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    // Check username
                    if (!rs.next()) {

                        response.sendRedirect(
                                request.getContextPath()
                                + "/login.html?error=1");
                        return;
                    }

                    String storedHash =
                            rs.getString("password_hash");

                    boolean isHashed = false;
                    boolean passwordCorrect = false;

                    // Verify password
                    if (storedHash != null
                            && !storedHash.isEmpty()) {

                        isHashed =
                                storedHash.split(":", -1).length == 3;

                        if (isHashed) {

                            passwordCorrect =
                                    PasswordUtil.verifyPassword(
                                            password, storedHash);

                        } else {

                            passwordCorrect =
                                    password.equals(storedHash);
                        }
                    }

                    // Wrong password
                    if (!passwordCorrect) {

                        response.sendRedirect(
                                request.getContextPath()
                                + "/login.html?error=1");
                        return;
                    }

                    // Convert old password to hash
                    if (!isHashed) {

                        String newHash =
                                PasswordUtil.hashPassword(password);

                        String updateSql =
                                "UPDATE " + table
                                + " SET password_hash = ? WHERE "
                                + usernameColumn + " = ?";

                        try (PreparedStatement update =
                                con.prepareStatement(updateSql)) {

                            update.setString(1, newHash);
                            update.setString(2, username);

                            update.executeUpdate();
                        }
                    }

                    // Lecturer information
                    int lecturerId = 0;
                    String lecturerName = null;

                    if ("lecturer".equals(role)) {

                        lecturerId = rs.getInt("lecturer_id");
                        lecturerName =
                                rs.getString("full_name");
                    }

                    // Destroy old session
                    HttpSession oldSession =
                            request.getSession(false);

                    if (oldSession != null) {
                        oldSession.invalidate();
                    }

                    // Create new session
                    HttpSession session =
                            request.getSession(true);

                    session.setAttribute("role", role);
                    session.setAttribute("username", username);

                    session.setMaxInactiveInterval(30 * 60);

                    // Admin dashboard
                    if ("admin".equals(role)) {

                        response.sendRedirect(
                                request.getContextPath()
                                + "/admin-dashboard.html");

                    } else {

                        // Lecturer session
                        session.setAttribute(
                                "lecturer_id", lecturerId);

                        session.setAttribute(
                                "lecturer_name", lecturerName);

                        response.sendRedirect(
                                request.getContextPath()
                                + "/lecturer-dashboard.html");
                    }
                }
            }

        } catch (Exception e) {

            log("Login error", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Login error. Please check the server log.");
        }
    }
}
