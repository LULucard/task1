package ru.vsu.cs.tatarinov.presentation.web.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "PhotoServlet", value = "/api/photos/*")
public class PhotoServlet extends HttpServlet {

    private List<Map<String, Object>> photos = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        // Добавляем тестовые фотографии
        addPhoto("Отдых на море", "user1", "beach.jpg");
        addPhoto("Горный поход", "admin", "mountain.jpg");
        addPhoto("Городская архитектура", "user2", "city.jpg");
    }

    private void addPhoto(String title, String author, String filename) {
        Map<String, Object> photo = new HashMap<>();
        photo.put("id", UUID.randomUUID().toString());
        photo.put("title", title);
        photo.put("author", author);
        photo.put("filename", filename);
        photo.put("uploaded", new java.util.Date());
        photo.put("likes", 0);
        photo.put("comments", new ArrayList<String>());
        photos.add(photo);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/photos - все фотографии
            JSONArray photosArray = new JSONArray();
            for (Map<String, Object> photo : photos) {
                photosArray.put(new JSONObject(photo));
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("count", photos.size());
            result.put("photos", photosArray);

            out.println(result.toString(2));
        } else {
            // GET /api/photos/{id} - конкретная фотография
            String photoId = pathInfo.substring(1);
            Map<String, Object> foundPhoto = null;

            for (Map<String, Object> photo : photos) {
                if (photoId.equals(photo.get("id"))) {
                    foundPhoto = photo;
                    break;
                }
            }

            if (foundPhoto != null) {
                JSONObject result = new JSONObject();
                result.put("status", "success");
                result.put("photo", new JSONObject(foundPhoto));
                out.println(result.toString(2));
            } else {
                response.setStatus(404);
                JSONObject error = new JSONObject();
                error.put("status", "error");
                error.put("message", "Фотография не найдена");
                out.println(error.toString(2));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(401);
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("message", "Требуется авторизация");
            out.println(error.toString(2));
            return;
        }

        String username = (String) session.getAttribute("username");
        String title = request.getParameter("title");

        if (title == null || title.trim().isEmpty()) {
            response.setStatus(400);
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("message", "Поле 'title' обязательно");
            out.println(error.toString(2));
            return;
        }

        // Создаем новую фотографию
        Map<String, Object> newPhoto = new HashMap<>();
        newPhoto.put("id", UUID.randomUUID().toString());
        newPhoto.put("title", title.trim());
        newPhoto.put("author", username);
        newPhoto.put("filename", "photo_" + System.currentTimeMillis() + ".jpg");
        newPhoto.put("uploaded", new java.util.Date());
        newPhoto.put("likes", 0);
        newPhoto.put("comments", new ArrayList<String>());

        photos.add(newPhoto);

        JSONObject result = new JSONObject();
        result.put("status", "success");
        result.put("message", "Фотография успешно загружена");
        result.put("photo", new JSONObject(newPhoto));

        response.setStatus(201);
        out.println(result.toString(2));
    }
}
