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

@WebServlet("/edit-attendance")
public class EditAttendanceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean isLoggedIn(HttpSession session) {
        return session != null
                && ("lecturer".equals(session.getAttribute("role"))
                || "admin".equals(session.getAttribute("role")));
    }

    private void showHeader(PrintWriter out, String title,
            String contextPath) {

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");

        out.println("<style>");
        out.println("*{box-sizing:border-box;}");
        out.println("body{margin:0;font-family:Arial,sans-serif;background:#f2f5f9;color:#263238;}");
        out.println(".header{background:#123b63;color:white;padding:20px;text-align:center;}");
        out.println(".logo{width:85px;height:85px;object-fit:contain;background:white;border-radius:50%;padding:5px;}");
        out.println(".nav{background:#092d4d;padding:13px;text-align:center;}");
        out.println(".nav a{color:white;text-decoration:none;margin:0 12px;font-weight:bold;}");
        out.println(".container{max-width:600px;margin:40px auto;padding:25px;background:white;border-radius:12px;box-shadow:0 3px 15px rgba(0,0,0,.10);}");
        out.println("h2{text-align:center;color:#123b63;}");
        out.println(".info{background:#f2f5f9;padding:12px;border-radius:7px;margin:12px 0;}");
        out.println("label{display:block;margin:18px 0 8px;font-weight:bold;}");
        out.println("select{width:100%;padding:12px;border:1px solid #bbb;border-radius:6px;font-size:16px;}");
        out.println("button{width:100%;padding:13px;margin-top:20px;background:#123b63;color:white;border:0;border-radius:6px;font-size:16px;cursor:pointer;}");
        out.println(".back{display:block;text-align:center;margin-top:20px;color:#123b63;text-decoration:none;font-weight:bold;}");
        out.println("@media(max-width:600px){.container{margin:20px 12px;padding:18px;}.nav a{display:inline-block;margin:7px;}}");
        out.println("</style></head><body>");

        out.println("<div class='header'>");
        out.println("<img class='logo' src='" + contextPath + "/images/obu-logo.png' alt='OBU Logo'>");
        out.println("<h2>ODA BULTUM UNIVERSITY</h2>");
        out.println("<p>Attendance Management System</p></div>");

        out.println("<div class='nav'>");
        out.println("<a href='" + contextPath + "/attendance-report'>Attendance Report</a>");
        out.println("<a href='" + contextPath + "/logout'>Logout</a>");
        out.println("</div>");
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (!isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idValue = request.getParameter("id");
 if (idValue == null || idValue.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Attendance ID is missing.");
            return;
        }

        try {
            int attendanceId = Integer.parseInt(idValue);

            if (attendanceId <= 0) throw new NumberFormatException();

            try (Connection con = DBConnection.getConnection()) {

                if (con == null) throw new SQLException("Database connection failed.");

                String sql = "SELECT a.attendance_id, s.student_number, "
                        + "s.full_name, a.attendance_date, a.status "
                        + "FROM attendance a "
                        + "JOIN students s ON a.student_id=s.student_id "
                        + "WHERE a.attendance_id=?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, attendanceId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            PrintWriter out = response.getWriter();
                            String status = rs.getString("status");
                            String contextPath = request.getContextPath();

                            showHeader(out, "Edit Attendance", contextPath);

                            out.println("<div class='container'>");
                            out.println("<h2>EDIT ATTENDANCE</h2>");

                            out.println("<div class='info'><b>Student Number:</b> "
                                    + escapeHtml(rs.getString("student_number")) + "</div>");
                            out.println("<div class='info'><b>Full Name:</b> "
                                    + escapeHtml(rs.getString("full_name")) + "</div>");
                            out.println("<div class='info'><b>Date:</b> "
                                    + rs.getDate("attendance_date") + "</div>");

                            out.println("<form action='" + contextPath
                                    + "/edit-attendance' method='post'>");
                            out.println("<input type='hidden' name='id' value='"
                                    + attendanceId + "'>");
                            out.println("<label>Attendance Status:</label>");
                            out.println("<select name='status' required>");

                            out.println("<option value='Present' "
                                    + ("Present".equals(status) ? "selected" : "")
                                    + ">Present</option>");
                            out.println("<option value='Absent' "
                                    + ("Absent".equals(status) ? "selected" : "")
                                    + ">Absent</option>");

                            out.println("</select>");
                            out.println("<button type='submit'>Update Attendance</button>");
                            out.println("</form>");

                            out.println("<a class='back' href='" + contextPath
                                    + "/attendance-report'>Back to Report</a>");
                            out.println("</div></body></html>");

                        } else {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                    "Attendance record not found.");
                        }
                    }
                }
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid Attendance ID.");
        } catch (Exception e) {
            log("Edit attendance loading error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load attendance record. Check server log.");
        }
    }
 @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String idValue = request.getParameter("id");
        String status = request.getParameter("status");

        if (idValue == null || idValue.trim().isEmpty()
                || (!"Present".equals(status) && !"Absent".equals(status))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid attendance information.");
            return;
        }

        try {
            int attendanceId = Integer.parseInt(idValue);

            if (attendanceId <= 0) throw new NumberFormatException();

            try (Connection con = DBConnection.getConnection()) {

                if (con == null) throw new SQLException("Database connection failed.");

                String sql = "UPDATE attendance SET status=? WHERE attendance_id=?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, status);
                    ps.setInt(2, attendanceId);

                    int updated = ps.executeUpdate();

                    if (updated > 0) {
                        response.sendRedirect(request.getContextPath()
                                + "/attendance-report");
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                "Attendance record not found.");
                    }
                }
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid Attendance ID.");
        } catch (Exception e) {
            log("Attendance update error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Update failed. Please check server log.");
        }
    }
}