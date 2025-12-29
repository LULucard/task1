package ru.vsu.cs.tatarinov.presentation.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/api/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Исключаем публичные эндпоинты из проверки
        String path = httpRequest.getRequestURI();
        if (path.contains("/api/users") && httpRequest.getMethod().equals("GET") &&
                (path.equals("/api/users") || path.matches("/api/users/\\d+"))) {
            // Разрешаем GET запросы к пользователям без авторизации
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            httpResponse.setStatus(401);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"status\":\"error\",\"message\":\"Требуется авторизация\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Инициализация фильтра
    }

    @Override
    public void destroy() {
        // Очистка ресурсов
    }
}