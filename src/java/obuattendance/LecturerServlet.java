
package obuattendance;

import java.io.IOException;
import java.io.PrintWriter;
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

@WebServlet("/lecturers")
public class LecturerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private boolean isAdmin(HttpSession session) {
        return session != null
                && "admin".equals(session.getAttribute("role"));
    }

    private boolean isLoggedIn(HttpSession session) {
        return session != null
                && ("admin".equals(session.getAttribute("role"))
                || "lecturer".equals(session.getAttribute("role")));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (!isLoggedIn(session)) {
            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        boolean admin = isAdmin(session);

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            String sql = "SELECT lecturer_id, full_name, email, department "
                    + "FROM lecturers ORDER BY lecturer_id";

            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                PrintWriter out = response.getWriter();

                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<meta name='viewport' "
                        + "content='width=device-width, initial-scale=1.0'>");
                out.println("<title>Lecturer Management - OBU</title>");

                out.println("<style>");
                out.println("*{box-sizing:border-box;}");
                out.println("body{font-family:Arial,sans-serif;"
                        + "background:#f1f5f9;margin:0;color:#1e293b;"
                        + "padding:20px;}");
                out.println(".header{text-align:center;"
                        + "background:linear-gradient(135deg,#123b65,#16804a);"
                        + "color:white;padding:25px;border-radius:10px;}");
                out.println(".logo{width:90px;height:auto;margin-bottom:10px;}");
                out.println(".container{max-width:1100px;margin:25px auto;"
                        + "background:white;padding:25px;border-radius:10px;"
                        + "box-shadow:0 3px 12px rgba(0,0,0,0.1);}");
                out.println(".table-container{width:100%;overflow-x:auto;}");
                out.println("table{width:100%;border-collapse:collapse;"
                        + "margin-top:20px;}");
                out.println("th{background:#123b65;color:white;padding:13px;}");
                out.println("td{padding:12px;text-align:center;"
                        + "border:1px solid #e2e8f0;}");
                out.println("tr:nth-child(even){background:#f8fafc;}");
                out.println(".link{display:inline-block;padding:10px 15px;"
                        + "border-radius:5px;background:#16804a;color:white;"
                        + "text-decoration:none;margin:5px;}");
                out.println(".link:hover{background:#116638;}");
                out.println("button{padding:9px 14px;border:0;"
                        + "border-radius:5px;background:#b22222;"
                        + "color:white;cursor:pointer;}");
                out.println(".links{text-align:center;margin-top:20px;}");
                out.println("@media(max-width:700px){"
                        + ".container{padding:12px;}table{font-size:12px;}"
                        + "th,td{padding:7px;}.header h1{font-size:22px;}"
                        + ".link{padding:8px 10px;}}");
                out.println("</style>");

                out.println("</head>");
                out.println("<body>");

                out.println("<div class='header'>");
                out.println("<img src='" + request.getContextPath()
                        + "/images/obu-logo.png' class='logo' alt='OBU Logo'>");
                out.println("<h1>ODA BULTUM UNIVERSITY</h1>");
                out.println("<h2>LECTURER MANAGEMENT</h2>");
                out.println("</div>");

                out.println("<div class='container'>");
                out.println("<p><strong>Role:</strong> "
                        + escapeHtml((String) session.getAttribute("role"))
                        + "</p>");

                out.println("<div class='table-container'>");
                out.println("<table>");
                out.println("<tr>");
                out.println("<th>ID</th>");
                out.println("<th>Full Name</th>");
                out.println("<th>Email</th>");
                out.println("<th>Department</th>");

                if (admin) {
                    out.println("<th>Action</th>");
                }

                out.println("</tr>");

                boolean hasLecturers = false;

                while (rs.next()) {
                    hasLecturers = true;

                    int id = rs.getInt("lecturer_id");

                    out.println("<tr>");
                    out.println("<td>" + id + "</td>");
                    out.println("<td>"
                            + escapeHtml(rs.getString("full_name"))
                            + "</td>");
                    out.println("<td>"
                            + escapeHtml(rs.getString("email"))
                            + "</td>");
                    out.println("<td>"
                            + escapeHtml(rs.getString("department"))
                            + "</td>");

                    if (admin) {
                        out.println("<td>");
                        out.println("<form action='"
                                + request.getContextPath()
                                + "/lecturers' method='post' "
                                + "onsubmit=\"return confirm("
                                + "'Are you sure you want to delete "
                                + "this lecturer?');\">");

                        out.println("<input type='hidden' "
                                + "name='action' value='delete'>");
                        out.println("<input type='hidden' "
                                + "name='lecturer_id' value='" + id + "'>");
                        out.println("<button type='submit'>Delete</button>");
                        out.println("</form>");
                        out.println("</td>");
                    }

                    out.println("</tr>");
                }

                if (!hasLecturers) {
                    out.println("<tr><td colspan='"
                            + (admin ? "5" : "4")
                            + "'>No lecturers found.</td></tr>");
                }

                out.println("</table>");
                out.println("</div>");

                out.println("<div class='links'>");

                if (admin) {
                    out.println("<a class='link' href='"
                            + request.getContextPath()
                            + "/add-lecturer.html'>Add Lecturer</a>");

                    out.println("<a class='link' href='"
                            + request.getContextPath()
                            + "/edit-lecturer.html'>Edit Lecturer</a>");

                    out.println("<a class='link' href='"
                            + request.getContextPath()
                            + "/admin-dashboard.html'>Back to Dashboard</a>");
                } else {
                    out.println("<a class='link' href='"
                            + request.getContextPath()
                            + "/lecturer-dashboard.html'>"
                            + "Back to Dashboard</a>");
                }

                out.println("<a class='link' href='"
                        + request.getContextPath()
                        + "/logout'>Logout</a>");

                out.println("</div>");
                out.println("</div>");
                out.println("</body>");
                out.println("</html>");
            }

        } catch (Exception e) {
            log("Lecturer list error", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error while loading lecturers.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isAdmin(session)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Access denied. Admin only.");
            return;
        }

        String action = request.getParameter("action");
        String fullName = request.getParameter("full_name");
        String email = request.getParameter("email");
        String department = request.getParameter("department");
        String password = request.getParameter("password");
        String lecturerId = request.getParameter("lecturer_id");

        String sql;

        if ("update".equals(action)) {

            if (isEmpty(fullName) || isEmpty(email)
                    || isEmpty(department) || isEmpty(lecturerId)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Missing lecturer details.");
                return;
            }

            sql = "UPDATE lecturers SET full_name=?, email=?, "
                    + "department=? WHERE lecturer_id=?";

        } else if ("delete".equals(action)) {

            if (isEmpty(lecturerId)) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Lecturer ID is required.");
                return;
            }

            sql = "DELETE FROM lecturers WHERE lecturer_id=?";

        } else if ("add".equals(action)) {

            if (isEmpty(fullName) || isEmpty(email)
                    || isEmpty(department) || isEmpty(password)
                    || password.length() < 8) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Enter valid details and a password "
                        + "of at least 8 characters.");
                return;
            }

            sql = "INSERT INTO lecturers "
                    + "(full_name, email, department, password_hash) "
                    + "VALUES (?, ?, ?, ?)";

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action.");
            return;
        }

        try {
            int id = 0;

            if ("update".equals(action) || "delete".equals(action)) {
                id = Integer.parseInt(lecturerId);

                if (id <= 0) {
                    throw new NumberFormatException();
                }
            }

            try (Connection con = DBConnection.getConnection()) {

                if (con == null) {
                    throw new SQLException("Database connection failed.");
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    if ("update".equals(action)) {
                        ps.setString(1, fullName.trim());
                        ps.setString(2, email.trim());
                        ps.setString(3, department.trim());
                        ps.setInt(4, id);

                    } else if ("delete".equals(action)) {
                        ps.setInt(1, id);

                    } else {
                        String hashedPassword =
                                PasswordUtil.hashPassword(password);

                        ps.setString(1, fullName.trim());
                        ps.setString(2, email.trim());
                        ps.setString(3, department.trim());
                        ps.setString(4, hashedPassword);
                    }

                    int result = ps.executeUpdate();

                    if (result == 0) {
                        response.getWriter().println(
                                "Lecturer not found or no changes made!");
                        return;
                    }
                }
            }

            response.sendRedirect(
                    request.getContextPath() + "/lecturers");

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid Lecturer ID.");

        } catch (Exception e) {
            log("Lecturer operation error", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Operation failed. Please check server log.");
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
