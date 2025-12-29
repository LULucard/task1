package ru.vsu.cs.tatarinov.presentation.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "HomeServlet", value = {"/", "/home"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        boolean isAuthenticated = session != null && session.getAttribute("user") != null;
        String username = isAuthenticated ? (String) session.getAttribute("username") : "Гость";

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Social Network - Главная</title><meta charset='UTF-8'></head>");
        out.println("<body style='font-family: Arial; padding: 30px; max-width: 800px; margin: 0 auto;'>");
        out.println("<h1>🚀 Social Network Platform</h1>");
        out.println("<p><strong>Статус:</strong> " + (isAuthenticated ? "✅ Авторизован" : "❌ Не авторизован") + "</p>");
        out.println("<p><strong>Пользователь:</strong> " + username + "</p>");
        out.println("<p><strong>Время сервера:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "</p>");
        out.println("<hr>");

        out.println("<h3>📋 Доступные API эндпоинты:</h3>");
        out.println("<div style='background: #f5f5f5; padding: 15px; border-radius: 8px;'>");
        out.println("<ul>");
        out.println("<li><strong>GET</strong> <a href='/login'>/login</a> - Форма входа</li>");
        out.println("<li><strong>POST</strong> /login - Отправка данных входа</li>");
        out.println("<li><strong>GET</strong> <a href='/logout'>/logout</a> - Выход из системы</li>");
        out.println("<li><strong>GET</strong> <a href='/api/users'>/api/users</a> - Список пользователей (JSON)</li>");
        out.println("<li><strong>GET</strong> <a href='/api/users/1'>/api/users/{id}</a> - Пользователь по ID</li>");
        out.println("<li><strong>POST</strong> /api/users - Создать пользователя</li>");
        out.println("<li><strong>GET</strong> <a href='/api/photos'>/api/photos</a> - Фотографии</li>");
        out.println("<li><strong>GET</strong> <a href='/api/relationships'>/api/relationships</a> - Отношения/друзья</li>");
        out.println("</ul>");
        out.println("</div>");

        out.println("<hr>");
        out.println("<h3>🔧 Техническая информация:</h3>");
        out.println("<p><strong>Метод запроса:</strong> " + request.getMethod() + "</p>");
        out.println("<p><strong>IP адрес:</strong> " + request.getRemoteAddr() + "</p>");
        out.println("<p><strong>Контекст приложения:</strong> " + request.getContextPath() + "</p>");

        if (isAuthenticated) {
            out.println("<div style='margin-top: 20px; padding: 15px; background: #e8f5e9; border-radius: 8px;'>");
            out.println("<h4>✅ Вы авторизованы</h4>");
            out.println("<p>ID сессии: " + session.getId().substring(0, 8) + "...</p>");
            out.println("<p><a href='/logout' style='color: #d32f2f;'>🚪 Выйти из системы</a></p>");
            out.println("</div>");
        } else {
            out.println("<div style='margin-top: 20px; padding: 15px; background: #fff3e0; border-radius: 8px;'>");
            out.println("<h4>🔐 Требуется авторизация</h4>");
            out.println("<p><a href='/login'>👉 Перейти к форме входа</a></p>");
            out.println("</div>");
        }

        out.println("</body></html>");
    }
}