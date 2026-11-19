package obuattendance;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/delete-attendance")
public class DeleteAttendanceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null
                || (!"lecturer".equals(session.getAttribute("role"))
                && !"admin".equals(session.getAttribute("role")))) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        String idValue = request.getParameter("id");

        if (idValue == null || idValue.trim().isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Attendance ID is missing.");
            return;
        }

        try {
            int attendanceId = Integer.parseInt(idValue);

            if (attendanceId <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid Attendance ID.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {

                if (con == null) {
                    throw new SQLException("Database connection failed.");
                }

                String sql =
                        "DELETE FROM attendance WHERE attendance_id=?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, attendanceId);

                    int deleted = ps.executeUpdate();

                    if (deleted > 0) {
                        response.sendRedirect(
                                request.getContextPath()
                                + "/attendance-report");
                    } else {
                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Attendance record not found.");
                    }
                }
            }

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid Attendance ID.");

        } catch (Exception e) {
            log("Delete attendance error", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Delete failed. Please check server log.");
        }
    }
}