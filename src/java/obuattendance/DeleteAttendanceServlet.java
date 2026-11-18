package obuattendance;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/delete-attendance")
public class DeleteAttendanceServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || !"lecturer".equals(session.getAttribute("role"))) {

            response.sendRedirect("login.html");
            return;
        }

        String idValue = request.getParameter("id");

        if (idValue == null || idValue.trim().isEmpty()) {
            response.getWriter().println("Attendance ID is missing.");
            return;
        }

        try {
            int attendanceId = Integer.parseInt(idValue);

            try (Connection con = DBConnection.getConnection()) {

                String sql =
                        "DELETE FROM Attendance WHERE attendance_id=?";

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {

                    ps.setInt(1, attendanceId);

                    int deleted = ps.executeUpdate();

                    if (deleted > 0) {
                        response.sendRedirect("attendance-report");
                    } else {
                        response.getWriter().println(
                                "Attendance record not found.");
                    }
                }
            }

        } catch (NumberFormatException e) {

            response.getWriter().println("Invalid Attendance ID.");

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                    "Delete error: " + e.getMessage());
        }
    }
}