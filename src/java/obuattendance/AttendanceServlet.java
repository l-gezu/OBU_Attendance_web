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

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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
            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        String contextPath = request.getContextPath();

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            var out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Take Attendance - OBU</title>");

            out.println("<style>");
            out.println("* { margin: 0; padding: 0; box-sizing: border-box; }");
            out.println("body { font-family: Arial, sans-serif; background: #f2f5f9; min-height: 100vh; color: #333; }");
            out.println(".header { background: linear-gradient(135deg, #123b65, #16804a); color: white; text-align: center; padding: 25px 15px; }");
            out.println(".logo { width: 85px; height: auto; margin-bottom: 10px; }");
            out.println(".header h1 { font-size: 25px; margin-bottom: 7px; }");
            out.println(".header p { font-size: 14px; opacity: 0.9; }");
            out.println(".container { width: 95%; max-width: 1100px; margin: 30px auto; background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }");
            out.println(".title { text-align: center; color: #123b65; font-size: 23px; margin-bottom: 10px; }");
            out.println(".subtitle { text-align: center; color: #777; margin-bottom: 25px; font-size: 14px; }");
            out.println(".table-wrapper { width: 100%; overflow-x: auto; }");
            out.println("table { width: 100%; border-collapse: collapse; min-width: 600px; }");
            out.println("th { background: #123b65; color: white; padding: 15px; text-align: left; font-size: 14px; }");
            out.println("td { padding: 13px 15px; border-bottom: 1px solid #e5e9ef; font-size: 14px; }");
            out.println("tr:hover td { background: #f5f9fc; }");
            out.println("select { padding: 9px 12px; border: 1px solid #ccc; border-radius: 6px; font-size: 14px; width: 130px; background: white; }");
            out.println("select:focus { outline: none; border-color: #16804a; }");
 out.println(".save-btn { display: block; width: 100%; max-width: 250px; margin: 25px auto 10px; padding: 14px; background: #16804a; color: white; border: none; border-radius: 7px; font-size: 16px; font-weight: bold; cursor: pointer; }");
            out.println(".save-btn:hover { background: #11663b; }");
            out.println(".navigation { display: flex; justify-content: center; flex-wrap: wrap; gap: 12px; margin-top: 25px; }");
            out.println(".navigation a { text-decoration: none; padding: 11px 17px; border-radius: 6px; background: #123b65; color: white; font-size: 14px; }");
            out.println(".navigation a:hover { background: #16804a; }");
            out.println(".empty { text-align: center; padding: 30px; color: #777; }");
            out.println("footer { text-align: center; padding: 20px; color: #777; font-size: 13px; }");
            out.println("@media(max-width: 600px) { .header h1 { font-size: 20px; } .container { padding: 15px; margin: 18px auto; } .title { font-size: 20px; } }");
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
                    + "FROM students ORDER BY student_id";

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
                    out.println("<option value='Present'>Present</option>");
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

            out.println("<button type='submit' class='save-btn'>Save Attendance</button>");
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
            out.println("<footer>OBU Attendance Management System | Academic Year 2026</footer>");
            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            log("Attendance page error", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error while loading attendance page.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isLecturer(session)) {
            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        Object lecturerObject = session.getAttribute("lecturer_id");

        if (!(lecturerObject instanceof Integer)) {
            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        Integer lecturerId = (Integer) lecturerObject;

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            con.setAutoCommit(false);

            try {
                String studentSql =
                        "SELECT student_id FROM students";

                String updateSql =
                        "UPDATE attendance SET lecturer_id=?, status=? "
                        + "WHERE student_id=? "
                        + "AND attendance_date=CURRENT_DATE";

                String insertSql =
                        "INSERT INTO attendance "
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
                                || (!status.equals("Present")
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
 response.sendRedirect(
                    request.getContextPath() + "/attendance-report");

        } catch (Exception e) {
            log("Attendance saving error", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Attendance saving failed. Please check server log.");
        }
    }
}