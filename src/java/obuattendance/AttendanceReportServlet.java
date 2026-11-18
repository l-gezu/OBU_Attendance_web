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

@WebServlet("/attendance-report")
public class AttendanceReportServlet extends HttpServlet {

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

        if (session == null
                || (!"lecturer".equals(session.getAttribute("role"))
                && !"admin".equals(session.getAttribute("role")))) {

            response.sendRedirect("login.html");
            return;
        }

        int totalStudents = 0;
        int totalPresent = 0;
        int totalAbsent = 0;
        int totalRecords = 0;
        double percentage = 0;

        try (Connection con = DBConnection.getConnection()) {

            // COUNT TOTAL STUDENTS
            String countStudents =
                    "SELECT COUNT(*) FROM Students";

            try (PreparedStatement ps =
                    con.prepareStatement(countStudents);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalStudents = rs.getInt(1);
                }
            }

            // ATTENDANCE SUMMARY
            String summarySQL =
                    "SELECT COUNT(*) AS total_records, "
                    + "COALESCE(SUM(CASE WHEN status='Présent' "
                    + "THEN 1 ELSE 0 END), 0) AS total_present, "
                    + "COALESCE(SUM(CASE WHEN status='Absent' "
                    + "THEN 1 ELSE 0 END), 0) AS total_absent "
                    + "FROM Attendance";

            try (PreparedStatement ps =
                    con.prepareStatement(summarySQL);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalRecords = rs.getInt("total_records");
                    totalPresent = rs.getInt("total_present");
                    totalAbsent = rs.getInt("total_absent");
                }
            }

            // CALCULATE PERCENTAGE
            if (totalRecords > 0) {
                percentage =
                        (totalPresent * 100.0) / totalRecords;
            }

            // SEARCH STUDENT
            String search = request.getParameter("search");

            String keyword =
                    (search == null) ? "" : search.trim();

            // FILTER BY DATE
            String date = request.getParameter("date");

            String sql =
                    "SELECT a.attendance_id, s.student_number, "
                    + "s.full_name, a.attendance_date, a.status "
                    + "FROM Attendance a "
                    + "JOIN Students s "
                    + "ON a.student_id = s.student_id "
                    + "WHERE (s.full_name LIKE ? "
                    + "OR s.student_number LIKE ?) "
                    + "AND (? IS NULL OR a.attendance_date = ?) "
                    + "ORDER BY a.attendance_date DESC";

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setString(1, "%" + keyword + "%");
                ps.setString(2, "%" + keyword + "%");

                if (date == null || date.trim().isEmpty()) {
 ps.setNull(3, java.sql.Types.DATE);
                    ps.setNull(4, java.sql.Types.DATE);

                } else {

                    java.sql.Date selectedDate =
                            java.sql.Date.valueOf(date);

                    ps.setDate(3, selectedDate);
                    ps.setDate(4, selectedDate);
                }

                try (ResultSet rs = ps.executeQuery()) {

                    var out = response.getWriter();

                    out.println("<!DOCTYPE html>");
                    out.println("<html lang='en'>");

                    out.println("<head>");
                    out.println("<meta charset='UTF-8'>");
                    out.println("<meta name='viewport' "
                            + "content='width=device-width, initial-scale=1.0'>");
                    out.println("<title>Attendance Report</title>");

                    out.println("<link rel='stylesheet' href='"
                            + request.getContextPath()
                            + "/attendance-report.css'>");

                    out.println("</head>");
                    out.println("<body>");

                    // UNIVERSITY HEADER
                    out.println("<div style='text-align:center;'>");

                    out.println("<img src='"
                            + request.getContextPath()
                            + "/images/obu-logo.png' "
                            + "alt='OBU Logo' "
                            + "style='width:100px;height:auto;'>");

                    out.println("<h1>ODA BULTUM UNIVERSITY</h1>");

                    out.println("</div>");

                    out.println("<h2>STUDENT ATTENDANCE REPORT</h2>");

                    // ATTENDANCE SUMMARY
                    out.println("<h3>ATTENDANCE SUMMARY</h3>");

                    out.println("<p>Total Students: "
                            + totalStudents + "</p>");

                    out.println("<p>Total Attendance Records: "
                            + totalRecords + "</p>");

                    out.println("<p>Total Present: "
                            + totalPresent + "</p>");

                    out.println("<p>Total Absent: "
                            + totalAbsent + "</p>");

                    out.println("<p>Attendance Percentage: "
                            + String.format("%.2f", percentage)
                            + "%</p>");

                    // SEARCH AND DATE FILTER
                    out.println("<h3>ATTENDANCE DETAILS</h3>");

                    out.println("<form action='"
                            + request.getContextPath()
                            + "/attendance-report' method='get'>");

                    out.println("<label>Search Student: </label>");

                    out.println("<input type='text' name='search' "
                            + "placeholder='Enter name or student number' "
                            + "value='" + escapeHtml(keyword) + "'>");

                    out.println("<br><br>");

                    out.println("<label>Filter by Date: </label>");

                    out.println("<input type='date' name='date' "
                            + "value='" + escapeHtml(date) + "'>");

                    out.println("<button type='submit'>Search</button>");

                    out.println("</form>");

                    out.println("<br>");

                    // ATTENDANCE TABLE
                    out.println("<table>");

                    out.println("<tr>");
                    out.println("<th>Student Number</th>");
                    out.println("<th>ID Number</th>");
                    out.println("<th>Full Name</th>");
                    out.println("<th>Date</th>");
                    out.println("<th>Status</th>");
                    out.println("<th>Action</th>");
                    out.println("</tr>");

                    while (rs.next()) {

                        int attendanceId =
                                rs.getInt("attendance_id");
 String studentNumber =
                                rs.getString("student_number");

                        String fullName =
                                rs.getString("full_name");

                        String status =
                                rs.getString("status");

                        out.println("<tr>");

                        // STUDENT NUMBER: Attendance ID
                        out.println("<td>"
                                + attendanceId
                                + "</td>");

                        // ID NUMBER: University Student Number
                        out.println("<td>"
                                + escapeHtml(studentNumber)
                                + "</td>");

                        out.println("<td>"
                                + escapeHtml(fullName)
                                + "</td>");

                        out.println("<td>"
                                + rs.getDate("attendance_date")
                                + "</td>");

                        out.println("<td>"
                                + escapeHtml(status)
                                + "</td>");

                        // EDIT AND DELETE
                        out.println("<td>");

                        out.println("<a href='edit-attendance?id="
                                + attendanceId
                                + "'>Edit</a>");

                        out.println(" | ");

                        out.println("<form action='delete-attendance' "
                                + "method='post' "
                                + "style='display:inline;' "
                                + "onsubmit=\"return confirm("
                                + "'Are you sure you want to delete "
                                + "this attendance record?');\">");

                        out.println("<input type='hidden' name='id' "
                                + "value='" + attendanceId + "'>");

                        out.println("<button type='submit'>Delete</button>");

                        out.println("</form>");

                        out.println("</td>");

                        out.println("</tr>");
                    }

                    out.println("</table>");

                    out.println("<br><a href='attendance'>"
                            + "Take Attendance</a>");

                    out.println("<br><br>");

                    out.println("<a href='"
                            + ("admin".equals(session.getAttribute("role"))
                            ? "admin-dashboard.html"
                            : "lecturer-dashboard.html")
                            + "'>Back to Dashboard</a>");

                    out.println("</body>");
                    out.println("</html>");
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                    "Database error: " + e.getMessage());
        }
    }
}