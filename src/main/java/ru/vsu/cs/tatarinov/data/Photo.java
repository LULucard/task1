package ru.vsu.cs.tatarinov.data;

import java.util.ArrayList;
import java.util.List;

public class Photo {
    private int id;
    private int userId;
    private String fileName;
    private byte[] fileData;
    private int fileSize;
    private String mimeType;
    private List<Integer> likes;
    private List<Integer> dislikes;

    public Photo(int id, int userId, String fileName, byte[] fileData, int fileSize, String mimeType) {
        this.id = id;
        this.userId = userId;
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.likes = new ArrayList<>();
        this.dislikes = new ArrayList<>();
    }

    // Конструктор для создания новой фотографии
    public Photo(int userId, String fileName, byte[] fileData, String mimeType) {
        this(0, userId, fileName, fileData, fileData.length, mimeType);
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getFileName() { return fileName; }
    public byte[] getFileData() { return fileData; }
    public int getFileSize() { return fileSize; }
    public String getMimeType() { return mimeType; }
    public List<Integer> getLikes() { return likes; }
    public List<Integer> getDislikes() { return dislikes; }

    // Setters
    public void setLikes(List<Integer> likes) { this.likes = likes; }
    public void setDislikes(List<Integer> dislikes) { this.dislikes = dislikes; }

    public void addLike(int userId) {
        if (!likes.contains(userId)) {
            dislikes.remove(Integer.valueOf(userId));
            likes.add(userId);
        }
    }

    public void addDislike(int userId) {
        if (!dislikes.contains(userId)) {
            likes.remove(Integer.valueOf(userId));
            dislikes.add(userId);
        }
    }

    public void removeReaction(int userId) {
        likes.remove(Integer.valueOf(userId));
        dislikes.remove(Integer.valueOf(userId));
    }

    // Вспомогательные методы
    public String getFileExtension() {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    public boolean isImage() {
        String extension = getFileExtension();
        return extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("gif") ||
                extension.equals("bmp");
    }
}