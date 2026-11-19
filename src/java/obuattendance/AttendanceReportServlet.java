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

@WebServlet("/attendance-report")
public class AttendanceReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private String escapeHtml(String value) {
        if (value == null) return "";
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

        if (session == null
                || (!"lecturer".equals(session.getAttribute("role"))
                && !"admin".equals(session.getAttribute("role")))) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        int totalStudents = 0;
        int totalPresent = 0;
        int totalAbsent = 0;
        int totalRecords = 0;
        double percentage = 0;

        String keyword = request.getParameter("search");
        keyword = keyword == null ? "" : keyword.trim();

        String dateValue = request.getParameter("date");
        dateValue = dateValue == null ? "" : dateValue.trim();

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            String countSQL = "SELECT COUNT(*) FROM students";

            try (PreparedStatement ps = con.prepareStatement(countSQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalStudents = rs.getInt(1);
            }

            String summarySQL =
                    "SELECT COUNT(*) AS total_records, "
                    + "COALESCE(SUM(CASE WHEN status='Present' "
                    + "THEN 1 ELSE 0 END),0) AS total_present, "
                    + "COALESCE(SUM(CASE WHEN status='Absent' "
                    + "THEN 1 ELSE 0 END),0) AS total_absent "
                    + "FROM attendance";

            try (PreparedStatement ps = con.prepareStatement(summarySQL);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalRecords = rs.getInt("total_records");
                    totalPresent = rs.getInt("total_present");
                    totalAbsent = rs.getInt("total_absent");
                }
            }

            if (totalRecords > 0) {
                percentage = totalPresent * 100.0 / totalRecords;
            }

            String sql =
                    "SELECT a.attendance_id, s.student_number, "
                    + "s.full_name, a.attendance_date, a.status "
                    + "FROM attendance a "
                    + "JOIN students s ON a.student_id=s.student_id "
                    + "WHERE (s.full_name LIKE ? OR s.student_number LIKE ?) "
                    + "AND (? = '' OR a.attendance_date = ?) "
                    + "ORDER BY a.attendance_date DESC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, "%" + keyword + "%");
                ps.setString(2, "%" + keyword + "%");
                ps.setString(3, dateValue);

                if (dateValue.isEmpty()) {
                    ps.setDate(4, null);
                } else {
                    ps.setDate(4, java.sql.Date.valueOf(dateValue));
                }
 try (ResultSet rs = ps.executeQuery()) {

                    PrintWriter out = response.getWriter();

                    out.println("<!DOCTYPE html><html><head>");
                    out.println("<meta charset='UTF-8'>");
                    out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                    out.println("<title>Attendance Report</title>");
                    out.println("<link rel='stylesheet' href='"
                            + request.getContextPath()
                            + "/attendance-report.css'>");
                    out.println("</head><body>");

                    out.println("<h1>ODA BULTUM UNIVERSITY</h1>");
                    out.println("<h2>STUDENT ATTENDANCE REPORT</h2>");

                    out.println("<h3>ATTENDANCE SUMMARY</h3>");
                    out.println("<p>Total Students: " + totalStudents + "</p>");
                    out.println("<p>Total Attendance Records: " + totalRecords + "</p>");
                    out.println("<p>Total Present: " + totalPresent + "</p>");
                    out.println("<p>Total Absent: " + totalAbsent + "</p>");
                    out.println("<p>Attendance Percentage: "
                            + String.format("%.2f", percentage) + "%</p>");

                    out.println("<h3>ATTENDANCE DETAILS</h3>");
                    out.println("<form action='" + request.getContextPath()
                            + "/attendance-report' method='get'>");
                    out.println("<input type='text' name='search' "
                            + "placeholder='Search student' value='"
                            + escapeHtml(keyword) + "'>");
                    out.println("<input type='date' name='date' value='"
                            + escapeHtml(dateValue) + "'>");
                    out.println("<button type='submit'>Search</button>");
                    out.println("</form><br>");

                    out.println("<table border='1'>");
                    out.println("<tr><th>Attendance ID</th>"
                            + "<th>Student Number</th><th>Full Name</th>"
                            + "<th>Date</th><th>Status</th><th>Action</th></tr>");

                    while (rs.next()) {
                        int id = rs.getInt("attendance_id");

                        out.println("<tr>");
                        out.println("<td>" + id + "</td>");
                        out.println("<td>" + escapeHtml(
                                rs.getString("student_number")) + "</td>");
                        out.println("<td>" + escapeHtml(
                                rs.getString("full_name")) + "</td>");
                        out.println("<td>" + rs.getDate("attendance_date") + "</td>");
                        out.println("<td>" + escapeHtml(
                                rs.getString("status")) + "</td>");

                        out.println("<td><a href='"
                                + request.getContextPath()
                                + "/edit-attendance?id=" + id
                                + "'>Edit</a> | ");

                        out.println("<form action='"
                                + request.getContextPath()
                                + "/delete-attendance' method='post' "
                                + "style='display:inline;' "
                                + "onsubmit=\"return confirm('Delete this record?');\">");
                        out.println("<input type='hidden' name='id' value='"
                                + id + "'>");
                        out.println("<button type='submit'>Delete</button>");
                        out.println("</form></td></tr>");
                    }

                    out.println("</table><br>");
                    out.println("<a href='" + request.getContextPath()
                            + "/attendance'>Take Attendance</a><br><br>");
 String dashboard = "admin".equals(session.getAttribute("role"))
                            ? "/admin-dashboard.html"
                            : "/lecturer-dashboard.html";

                    out.println("<a href='" + request.getContextPath()
                            + dashboard + "'>Back to Dashboard</a>");

                    out.println("</body></html>");
                }
            }

        } catch (Exception e) {
            log("Attendance report database error", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error while loading attendance report.");
        }
    }
}