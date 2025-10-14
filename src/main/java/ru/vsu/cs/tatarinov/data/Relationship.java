package ru.vsu.cs.tatarinov.data;


public class Relationship {
    public enum RelationshipType {
        SUBSCRIPTION, FRIENDSHIP, BLOCKED
    }

    private int sourceUserId;
    private int targetUserId;
    private RelationshipType type;

    public Relationship(int sourceUserId, int targetUserId, RelationshipType type) {
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.type = type;
    }

    public int getSourceUserId() { return sourceUserId; }
    public int getTargetUserId() { return targetUserId; }
    public RelationshipType getType() { return type; }

    public void setType(RelationshipType type) { this.type = type; }
}