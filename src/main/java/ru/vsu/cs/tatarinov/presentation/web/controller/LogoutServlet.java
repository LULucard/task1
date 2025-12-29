package ru.vsu.cs.tatarinov.presentation.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", value = "/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate(); // Полное удаление сессии

            // Редирект с сообщением об успешном выходе
            response.sendRedirect(request.getContextPath() + "/login?success=Вы успешно вышли из системы" +
                    (username != null ? " (" + username + ")" : ""));
        } else {
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}