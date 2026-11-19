
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

@WebServlet("/students")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Check whether user is admin or lecturer
    private boolean isLoggedIn(HttpSession session) {
        return session != null &&
                ("admin".equals(session.getAttribute("role")) ||
                 "lecturer".equals(session.getAttribute("role")));
    }

    // Check whether user is admin
    private boolean isAdmin(HttpSession session) {
        return session != null &&
                "admin".equals(session.getAttribute("role"));
    }

    // DISPLAY STUDENTS
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
                throw new SQLException("Database connection failed");
            }

            String sql = "SELECT * FROM students ORDER BY student_id DESC";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                PrintWriter out = response.getWriter();

                out.println("<!DOCTYPE html>");
                out.println("<html><head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                out.println("<title>Student Management - OBU</title>");

                out.println("<style>");
                out.println("*{box-sizing:border-box;}");
                out.println("body{font-family:Arial,sans-serif;background:#f1f5f9;margin:0;color:#1e293b;padding:20px;}");
                out.println(".header{text-align:center;background:linear-gradient(135deg,#123b65,#16804a);color:white;padding:25px;border-radius:10px;}");
                out.println(".logo{width:90px;height:auto;margin-bottom:10px;}");
                out.println(".container{max-width:1100px;margin:25px auto;background:white;padding:25px;border-radius:10px;box-shadow:0 3px 12px rgba(0,0,0,0.1);}");
                out.println("table{width:100%;border-collapse:collapse;margin-top:20px;}");
                out.println("th{background:#123b65;color:white;padding:13px;}");
                out.println("td{padding:12px;text-align:center;border:1px solid #e2e8f0;}");
                out.println("tr:nth-child(even){background:#f8fafc;}");
                out.println("button,.link{display:inline-block;padding:10px 15px;border:0;border-radius:5px;background:#16804a;color:white;text-decoration:none;cursor:pointer;margin:5px;}");
                out.println(".delete{background:#b22222;}");
                out.println(".links{text-align:center;margin-top:20px;}");
                out.println("@media(max-width:700px){.container{overflow-x:auto;padding:12px;}table{font-size:12px;}th,td{padding:7px;}}");
                out.println("</style>");

                out.println("</head><body>");

                out.println("<div class='header'>");
                out.println("<img src='" + request.getContextPath()
                        + "/images/obu-logo.png' class='logo' alt='OBU Logo'>");
                out.println("<h1>ODA BULTUM UNIVERSITY</h1>");
                out.println("<h2>STUDENT MANAGEMENT</h2>");
                out.println("</div>");

                out.println("<div class='container'>");

                out.println("<p>Role: "
                        + escape(String.valueOf(session.getAttribute("role")))
                        + "</p>");

                out.println("<table>");
                out.println("<tr>");
                out.println("<th>Student ID</th>");
                out.println("<th>Student Number</th>");
                out.println("<th>Name</th>");
                out.println("<th>Department</th>");
                out.println("<th>Year Level</th>");

                if (admin) {
                    out.println("<th>Action</th>");
                }

                out.println("</tr>");

                boolean found = false;

                while (rs.next()) {

                    found = true;

                    String number = rs.getString("student_number");
                    String fullName = rs.getString("full_name");
                    String department = rs.getString("department");

                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("student_id") + "</td>");
                    out.println("<td>" + escape(number) + "</td>");
                    out.println("<td>" + escape(fullName) + "</td>");
                    out.println("<td>" + escape(department) + "</td>");
                    out.println("<td>" + rs.getInt("year_level") + "</td>");

                    if (admin) {
                        out.println("<td>");

                        out.println("<form action='" +
                                request.getContextPath() +
                                "/students' method='post' " +
                                "onsubmit=\"return confirm('Delete this student?');\">");

                        out.println("<input type='hidden' name='action' value='delete'>");

                        out.println("<input type='hidden' name='student_number' value='"
                                + escape(number) + "'>");

                        out.println("<button class='delete' type='submit'>Delete</button>");
                        out.println("</form>");
                        out.println("</td>");
                    }

                    out.println("</tr>");
                }

                if (!found) {
                    out.println("<tr><td colspan='" +
                            (admin ? "6" : "5") +
                            "'>No students found!</td></tr>");
                }

                out.println("</table>");
                out.println("<div class='links'>");

                if (admin) {
                    out.println("<a class='link' href='" +
                            request.getContextPath() +
                            "/add-student.html'>Add Student</a>");

                    out.println("<a class='link' href='" +
                            request.getContextPath() +
                            "/edit-student.html'>Edit Student</a>");

                    out.println("<a class='link' href='" +
                            request.getContextPath() +
                            "/admin-dashboard.html'>Dashboard</a>");
                } else {
                    out.println("<a class='link' href='" +
                            request.getContextPath() +
                            "/lecturer-dashboard.html'>Dashboard</a>");
                }

                out.println("<a class='link' href='" +
                        request.getContextPath() +
                        "/logout'>Logout</a>");

                out.println("</div>");
                out.println("</div>");
                out.println("</body></html>");
            }

        } catch (Exception e) {
            log("Student list error", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load students. Please check server logs.");
        }
    }

    // ADD, UPDATE AND DELETE STUDENTS
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
        String studentNumber = request.getParameter("student_number");

        String fullName = request.getParameter("full_name");
        String department = request.getParameter("department");
        String yearLevel = request.getParameter("year_level");

        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            response.sendError(400, "Student number is required.");
            return;
        }

        String sql;

        if ("update".equals(action)) {

            sql = "UPDATE students SET full_name=?, department=?, "
                    + "year_level=? WHERE student_number=?";

        } else if ("delete".equals(action)) {

            sql = "DELETE FROM students WHERE student_number=?";

        } else if ("add".equals(action)) {

            sql = "INSERT INTO students "
                    + "(student_number, full_name, department, year_level) "
                    + "VALUES (?, ?, ?, ?)";

        } else {
            response.sendError(400, "Invalid action.");
            return;
        }

        // Validate details for add and update
        if (!"delete".equals(action)) {

            if (fullName == null || department == null || yearLevel == null ||
                    fullName.trim().isEmpty() ||
                    department.trim().isEmpty() ||
                    yearLevel.trim().isEmpty()) {

                response.sendError(400, "Missing student details.");
                return;
            }
        }

        int year = 0;

        if (!"delete".equals(action)) {

            try {
                year = Integer.parseInt(yearLevel.trim());

                if (year < 1 || year > 6) {
                    response.sendError(400,
                            "Year level must be between 1 and 6.");
                    return;
                }

            } catch (NumberFormatException e) {
                response.sendError(400, "Invalid year level.");
                return;
            }
        }

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                if ("update".equals(action)) {

                    ps.setString(1, fullName.trim());
                    ps.setString(2, department.trim());
                    ps.setInt(3, year);
                    ps.setString(4, studentNumber.trim());

                } else if ("delete".equals(action)) {

                    ps.setString(1, studentNumber.trim());

                } else {

                    ps.setString(1, studentNumber.trim());
                    ps.setString(2, fullName.trim());
                    ps.setString(3, department.trim());
                    ps.setInt(4, year);
                }

                ps.executeUpdate();

                response.sendRedirect(
                        request.getContextPath() + "/students");
            }

        } catch (SQLException e) {

            log("Student database operation error", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database operation failed. Check server logs.");

        } catch (Exception e) {

            log("Student operation error", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Student operation failed.");
        }
    }

    // HTML OUTPUT ESCAPING
    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
