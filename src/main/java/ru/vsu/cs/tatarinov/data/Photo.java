package ru.vsu.cs.tatarinov.data;

import java.util.ArrayList;
import java.util.List;

public class Photo {
    private String filePath;
    private List<Integer> likes;
    private List<Integer> dislikes;

    public Photo(String filePath) {
        this.filePath = filePath;
        this.likes = new ArrayList<>();
        this.dislikes = new ArrayList<>();
    }

    public String getFilePath() { return filePath; }
    public List<Integer> getLikes() { return likes; }
    public List<Integer> getDislikes() { return dislikes; }

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
}