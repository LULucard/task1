package ru.vsu.cs.tatarinov.presentation.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "UserServlet", value = "/api/users/*")
public class UserServlet extends HttpServlet {

    private Map<Integer, Map<String, Object>> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public void init() throws ServletException {
        // Инициализируем тестовых пользователей
        addUser("Иван Петров", "ivan@example.com", "admin");
        addUser("Мария Сидорова", "maria@example.com", "user");
        addUser("Алексей Иванов", "alex@example.com", "user");
    }

    private void addUser(String name, String email, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", nextId);
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        user.put("created", new java.util.Date());
        users.put(nextId, user);
        nextId++;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/users - все пользователи
            JSONArray usersArray = new JSONArray();
            for (Map<String, Object> user : users.values()) {
                usersArray.put(new JSONObject(user));
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("count", users.size());
            result.put("users", usersArray);
            result.put("timestamp", new java.util.Date().getTime());

            out.println(result.toString(2)); // Pretty print with indentation
        } else {
            // GET /api/users/{id} - конкретный пользователь
            try {
                String idStr = pathInfo.substring(1);
                int id = Integer.parseInt(idStr);

                Map<String, Object> user = users.get(id);
                if (user != null) {
                    JSONObject result = new JSONObject();
                    result.put("status", "success");
                    result.put("user", new JSONObject(user));
                    out.println(result.toString(2));
                } else {
                    response.setStatus(404);
                    JSONObject error = new JSONObject();
                    error.put("status", "error");
                    error.put("message", "Пользователь с ID " + id + " не найден");
                    out.println(error.toString(2));
                }
            } catch (NumberFormatException e) {
                response.setStatus(400);
                JSONObject error = new JSONObject();
                error.put("status", "error");
                error.put("message", "Неверный формат ID");
                out.println(error.toString(2));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Читаем тело запроса (в реальном приложении здесь был бы JSON парсер)
            String name = request.getParameter("name");
            String email = request.getParameter("email");

            if (name == null || email == null || name.trim().isEmpty() || email.trim().isEmpty()) {
                response.setStatus(400);
                JSONObject error = new JSONObject();
                error.put("status", "error");
                error.put("message", "Поля 'name' и 'email' обязательны");
                out.println(error.toString(2));
                return;
            }

            // Создаем нового пользователя
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("id", nextId);
            newUser.put("name", name.trim());
            newUser.put("email", email.trim());
            newUser.put("role", "user");
            newUser.put("created", new java.util.Date());

            users.put(nextId, newUser);

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("message", "Пользователь успешно создан");
            result.put("user", new JSONObject(newUser));

            response.setStatus(201); // Created
            out.println(result.toString(2));

            nextId++;

        } catch (Exception e) {
            response.setStatus(500);
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("message", "Ошибка при создании пользователя: " + e.getMessage());
            out.println(error.toString(2));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            try {
                String idStr = pathInfo.substring(1);
                int id = Integer.parseInt(idStr);

                if (users.containsKey(id)) {
                    users.remove(id);

                    JSONObject result = new JSONObject();
                    result.put("status", "success");
                    result.put("message", "Пользователь с ID " + id + " удален");
                    out.println(result.toString(2));
                } else {
                    response.setStatus(404);
                    JSONObject error = new JSONObject();
                    error.put("status", "error");
                    error.put("message", "Пользователь с ID " + id + " не найден");
                    out.println(error.toString(2));
                }
            } catch (NumberFormatException e) {
                response.setStatus(400);
                JSONObject error = new JSONObject();
                error.put("status", "error");
                error.put("message", "Неверный формат ID");
                out.println(error.toString(2));
            }
        } else {
            response.setStatus(400);
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("message", "Не указан ID пользователя для удаления");
            out.println(error.toString(2));
        }
    }
}