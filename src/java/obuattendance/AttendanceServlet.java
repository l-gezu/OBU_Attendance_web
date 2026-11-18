 package obuattendance;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {

    private boolean isLecturer(HttpSession session) {
        return session != null
                && "lecturer".equals(session.getAttribute("role"));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (!isLecturer(session)) {
            response.sendRedirect("login.html");
            return;
        }

        String contextPath = request.getContextPath();

        try (Connection con = DBConnection.getConnection()) {

            var out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Take Attendance - OBU</title>");

            out.println("<style>");

            out.println("* {");
            out.println("margin: 0;");
            out.println("padding: 0;");
            out.println("box-sizing: border-box;");
            out.println("}");

            out.println("body {");
            out.println("font-family: Arial, sans-serif;");
            out.println("background: #f2f5f9;");
            out.println("min-height: 100vh;");
            out.println("color: #333;");
            out.println("}");

            out.println(".header {");
            out.println("background: linear-gradient(135deg, #123b65, #16804a);");
            out.println("color: white;");
            out.println("text-align: center;");
            out.println("padding: 25px 15px;");
            out.println("}");

            out.println(".logo {");
            out.println("width: 85px;");
            out.println("height: auto;");
            out.println("margin-bottom: 10px;");
            out.println("}");

            out.println(".header h1 {");
            out.println("font-size: 25px;");
            out.println("margin-bottom: 7px;");
            out.println("}");

            out.println(".header p {");
            out.println("font-size: 14px;");
            out.println("opacity: 0.9;");
            out.println("}");

            out.println(".container {");
            out.println("width: 95%;");
            out.println("max-width: 1100px;");
            out.println("margin: 30px auto;");
            out.println("background: white;");
            out.println("padding: 25px;");
            out.println("border-radius: 12px;");
            out.println("box-shadow: 0 4px 15px rgba(0,0,0,0.08);");
            out.println("}");

            out.println(".title {");
            out.println("text-align: center;");
            out.println("color: #123b65;");
            out.println("font-size: 23px;");
            out.println("margin-bottom: 10px;");
            out.println("}");

            out.println(".subtitle {");
            out.println("text-align: center;");
            out.println("color: #777;");
            out.println("margin-bottom: 25px;");
            out.println("font-size: 14px;");
            out.println("}");
 out.println(".table-wrapper {");
            out.println("width: 100%;");
            out.println("overflow-x: auto;");
            out.println("}");

            out.println("table {");
            out.println("width: 100%;");
            out.println("border-collapse: collapse;");
            out.println("min-width: 600px;");
            out.println("}");

            out.println("th {");
            out.println("background: #123b65;");
            out.println("color: white;");
            out.println("padding: 15px;");
            out.println("text-align: left;");
            out.println("font-size: 14px;");
            out.println("}");

            out.println("td {");
            out.println("padding: 13px 15px;");
            out.println("border-bottom: 1px solid #e5e9ef;");
            out.println("font-size: 14px;");
            out.println("}");

            out.println("tr:hover td {");
            out.println("background: #f5f9fc;");
            out.println("}");

            out.println("select {");
            out.println("padding: 9px 12px;");
            out.println("border: 1px solid #ccc;");
            out.println("border-radius: 6px;");
            out.println("font-size: 14px;");
            out.println("width: 130px;");
            out.println("background: white;");
            out.println("}");

            out.println("select:focus {");
            out.println("outline: none;");
            out.println("border-color: #16804a;");
            out.println("}");

            out.println(".save-btn {");
            out.println("display: block;");
            out.println("width: 100%;");
            out.println("max-width: 250px;");
            out.println("margin: 25px auto 10px;");
            out.println("padding: 14px;");
            out.println("background: #16804a;");
            out.println("color: white;");
            out.println("border: none;");
            out.println("border-radius: 7px;");
            out.println("font-size: 16px;");
            out.println("font-weight: bold;");
            out.println("cursor: pointer;");
            out.println("}");

            out.println(".save-btn:hover {");
            out.println("background: #11663b;");
            out.println("}");

            out.println(".navigation {");
            out.println("display: flex;");
            out.println("justify-content: center;");
            out.println("flex-wrap: wrap;");
            out.println("gap: 12px;");
            out.println("margin-top: 25px;");
            out.println("}");

            out.println(".navigation a {");
            out.println("text-decoration: none;");
            out.println("padding: 11px 17px;");
            out.println("border-radius: 6px;");
            out.println("background: #123b65;");
            out.println("color: white;");
            out.println("font-size: 14px;");
            out.println("}");

            out.println(".navigation a:hover {");
            out.println("background: #16804a;");
            out.println("}");

            out.println(".empty {");
            out.println("text-align: center;");
            out.println("padding: 30px;");
            out.println("color: #777;");
            out.println("}");

            out.println("footer {");
            out.println("text-align: center;");
            out.println("padding: 20px;");
            out.println("color: #777;");
            out.println("font-size: 13px;");
            out.println("}");

            out.println("@media(max-width: 600px) {");
            out.println(".header h1 { font-size: 20px; }");
            out.println(".container { padding: 15px; margin: 18px auto; }");
            out.println(".title { font-size: 20px; }");
            out.println("}");

            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
 out.println("<header class='header'>");
            out.println("<img src='" + contextPath
                    + "/images/obu-logo.png' alt='OBU Logo' class='logo'>");
            out.println("<h1>ODA BULTUM UNIVERSITY</h1>");
            out.println("<p>Attendance Management System</p>");
            out.println("</header>");

            out.println("<main class='container'>");

            out.println("<h2 class='title'>STUDENT ATTENDANCE</h2>");
            out.println("<p class='subtitle'>Select attendance status for each student</p>");

            out.println("<form action='" + contextPath
                    + "/attendance' method='post'>");

            out.println("<div class='table-wrapper'>");
            out.println("<table>");

            out.println("<tr>");
            out.println("<th>ID</th>");
            out.println("<th>Student Number</th>");
            out.println("<th>Full Name</th>");
            out.println("<th>Attendance Status</th>");
            out.println("</tr>");

            String sql = "SELECT student_id, student_number, full_name "
                    + "FROM Students ORDER BY student_id";

            int studentCount = 0;

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    studentCount++;

                    int id = rs.getInt("student_id");

                    out.println("<tr>");

                    out.println("<td>" + id + "</td>");

                    out.println("<td>"
                            + escapeHtml(rs.getString("student_number"))
                            + "</td>");

                    out.println("<td>"
                            + escapeHtml(rs.getString("full_name"))
                            + "</td>");

                    out.println("<td>");

                    out.println("<select name='status_" + id + "' required>");
                    out.println("<option value=''>Select Status</option>");
                    out.println("<option value='Présent'>Present</option>");
                    out.println("<option value='Absent'>Absent</option>");
                    out.println("</select>");

                    out.println("</td>");
                    out.println("</tr>");
                }
            }

            out.println("</table>");
            out.println("</div>");

            if (studentCount == 0) {
                out.println("<p class='empty'>No students found in the database.</p>");
            }

            out.println("<button type='submit' class='save-btn'>");
            out.println("Save Attendance");
            out.println("</button>");

            out.println("</form>");

            out.println("<div class='navigation'>");

            out.println("<a href='" + contextPath
                    + "/attendance-report'>View Attendance Report</a>");

            out.println("<a href='" + contextPath
                    + "/lecturer-dashboard.html'>Back to Dashboard</a>");

            out.println("<a href='" + contextPath
                    + "/logout'>Logout</a>");

            out.println("</div>");

            out.println("</main>");

            out.println("<footer>");
            out.println("OBU Attendance Management System | Academic Year 2026");
            out.println("</footer>");

            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            log("Attendance page error", e);
            response.getWriter().println("Database error.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isLecturer(session)) {
            response.sendRedirect("login.html");
            return;
        }

        Integer lecturerId =
                (Integer) session.getAttribute("lecturer_id");
 if (lecturerId == null) {
            response.sendRedirect("login.html");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                String studentSql =
                        "SELECT student_id FROM Students";

                String updateSql =
                        "UPDATE Attendance "
                        + "SET lecturer_id=?, status=? "
                        + "WHERE student_id=? "
                        + "AND attendance_date=CURRENT_DATE";

                String insertSql =
                        "INSERT INTO Attendance "
                        + "(student_id, lecturer_id, attendance_date, status) "
                        + "VALUES (?, ?, CURRENT_DATE, ?)";

                try (PreparedStatement studentPs =
                            con.prepareStatement(studentSql);
                     ResultSet rs = studentPs.executeQuery();
                     PreparedStatement updatePs =
                            con.prepareStatement(updateSql);
                     PreparedStatement insertPs =
                            con.prepareStatement(insertSql)) {

                    while (rs.next()) {

                        int studentId = rs.getInt("student_id");

                        String status = request.getParameter(
                                "status_" + studentId);

                        if (status == null
                                || (!status.equals("Présent")
                                && !status.equals("Absent"))) {
                            continue;
                        }

                        updatePs.setInt(1, lecturerId);
                        updatePs.setString(2, status);
                        updatePs.setInt(3, studentId);

                        int updated = updatePs.executeUpdate();

                        if (updated == 0) {

                            insertPs.setInt(1, studentId);
                            insertPs.setInt(2, lecturerId);
                            insertPs.setString(3, status);

                            insertPs.executeUpdate();
                        }
                    }
                }

                con.commit();

            } catch (Exception e) {

                con.rollback();
                throw e;
            }

            response.sendRedirect("attendance-report");

        } catch (Exception e) {

            log("Attendance saving error", e);

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(
                    "Attendance saving failed. Check server log.");
        }
    }
}