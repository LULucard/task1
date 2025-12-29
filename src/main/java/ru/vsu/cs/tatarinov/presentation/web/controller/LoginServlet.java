package ru.vsu.cs.tatarinov.presentation.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Вход - Social Network</title><meta charset='UTF-8'>");
        out.println("<style>");
        out.println("body { font-family: Arial; padding: 40px; max-width: 400px; margin: 0 auto; }");
        out.println(".login-form { background: #f9f9f9; padding: 25px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        out.println("input { width: 100%; padding: 10px; margin: 8px 0; border: 1px solid #ddd; border-radius: 5px; }");
        out.println("button { background: #4CAF50; color: white; padding: 12px; border: none; border-radius: 5px; width: 100%; cursor: pointer; }");
        out.println("</style>");
        out.println("</head><body>");

        out.println("<h2>🔐 Вход в Social Network</h2>");

        String error = request.getParameter("error");
        if (error != null) {
            out.println("<div style='color: #d32f2f; padding: 10px; background: #ffebee; border-radius: 5px; margin-bottom: 15px;'>");
            out.println("❌ " + error);
            out.println("</div>");
        }

        String success = request.getParameter("success");
        if (success != null) {
            out.println("<div style='color: #388e3c; padding: 10px; background: #e8f5e9; border-radius: 5px; margin-bottom: 15px;'>");
            out.println("✅ " + success);
            out.println("</div>");
        }

        out.println("<div class='login-form'>");
        out.println("<form method='POST' action='/login'>");
        out.println("<label>Имя пользователя:</label>");
        out.println("<input type='text' name='username' required placeholder='admin'>");
        out.println("<label>Пароль:</label>");
        out.println("<input type='password' name='password' required placeholder='123'>");
        out.println("<button type='submit'>Войти</button>");
        out.println("</form>");
        out.println("</div>");

        out.println("<p style='margin-top: 20px; text-align: center;'>");
        out.println("<a href='/home'>← Назад на главную</a>");
        out.println("</p>");

        out.println("<div style='margin-top: 25px; padding: 15px; background: #e3f2fd; border-radius: 8px; font-size: 0.9em;'>");
        out.println("<p><strong>Тестовые учетные данные:</strong></p>");
        out.println("<p>👤 <strong>admin</strong> / 123</p>");
        out.println("<p>👤 <strong>user1</strong> / 456</p>");
        out.println("</div>");

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Простая имитация проверки логина
        boolean isValid = false;
        if ("admin".equals(username) && "123".equals(password)) {
            isValid = true;
        } else if ("user1".equals(username) && "456".equals(password)) {
            isValid = true;
        }

        if (isValid) {
            HttpSession session = request.getSession();
            session.setAttribute("user", username);
            session.setAttribute("username", username);
            session.setMaxInactiveInterval(30 * 60); // 30 минут

            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?error=Неверные учетные данные");
        }
    }
}