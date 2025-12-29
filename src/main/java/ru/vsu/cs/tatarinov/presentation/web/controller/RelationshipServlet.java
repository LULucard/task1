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

@WebServlet(name = "RelationshipServlet", value = "/api/relationships/*")
public class RelationshipServlet extends HttpServlet {

    private Map<String, List<Map<String, Object>>> relationships = new HashMap<>();

    @Override
    public void init() throws ServletException {
        // Инициализируем тестовые отношения
        addRelationship("admin", "user1", "friend");
        addRelationship("admin", "user2", "follower");
        addRelationship("user1", "user2", "friend");
    }

    private void addRelationship(String user1, String user2, String type) {
        Map<String, Object> rel = new HashMap<>();
        rel.put("id", UUID.randomUUID().toString());
        rel.put("user1", user1);
        rel.put("user2", user2);
        rel.put("type", type);
        rel.put("created", new java.util.Date());
        rel.put("status", "active");

        relationships.computeIfAbsent(user1, k -> new ArrayList<>()).add(rel);
        relationships.computeIfAbsent(user2, k -> new ArrayList<>()).add(rel);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

        String currentUser = (String) session.getAttribute("username");
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/relationships - все отношения текущего пользователя
            List<Map<String, Object>> userRelations = relationships.getOrDefault(currentUser, new ArrayList<>());

            JSONArray relArray = new JSONArray();
            for (Map<String, Object> rel : userRelations) {
                relArray.put(new JSONObject(rel));
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("user", currentUser);
            result.put("count", userRelations.size());
            result.put("relationships", relArray);

            out.println(result.toString(2));
        } else if (pathInfo.equals("/friends")) {
            // GET /api/relationships/friends - только друзья
            List<Map<String, Object>> userRelations = relationships.getOrDefault(currentUser, new ArrayList<>());
            List<Map<String, Object>> friends = new ArrayList<>();

            for (Map<String, Object> rel : userRelations) {
                if ("friend".equals(rel.get("type")) && "active".equals(rel.get("status"))) {
                    friends.add(rel);
                }
            }

            JSONArray friendsArray = new JSONArray();
            for (Map<String, Object> friend : friends) {
                friendsArray.put(new JSONObject(friend));
            }

            JSONObject result = new JSONObject();
            result.put("status", "success");
            result.put("user", currentUser);
            result.put("friend_count", friends.size());
            result.put("friends", friendsArray);

            out.println(result.toString(2));
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

        String currentUser = (String) session.getAttribute("username");
        String targetUser = request.getParameter("target_user");
        String type = request.getParameter("type");

        if (targetUser == null || type == null) {
            response.setStatus(400);
            JSONObject error = new JSONObject();
            error.put("status", "error");
            error.put("message", "Поля 'target_user' и 'type' обязательны");
            out.println(error.toString(2));
            return;
        }

        // Создаем новое отношение
        Map<String, Object> newRel = new HashMap<>();
        newRel.put("id", UUID.randomUUID().toString());
        newRel.put("user1", currentUser);
        newRel.put("user2", targetUser);
        newRel.put("type", type);
        newRel.put("created", new java.util.Date());
        newRel.put("status", "pending");

        relationships.computeIfAbsent(currentUser, k -> new ArrayList<>()).add(newRel);
        relationships.computeIfAbsent(targetUser, k -> new ArrayList<>()).add(newRel);

        JSONObject result = new JSONObject();
        result.put("status", "success");
        result.put("message", "Запрос на отношение отправлен");
        result.put("relationship", new JSONObject(newRel));

        response.setStatus(201);
        out.println(result.toString(2));
    }
}