package com.scratchpad.model;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class Todo {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Todo() {}

    public Todo(Long id, String title, String description, boolean completed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put("id", id)
            .put("title", title)
            .put("description", description)
            .put("completed", completed)
            .put("createdAt", createdAt != null ? createdAt.toString() : null)
            .put("updatedAt", updatedAt != null ? updatedAt.toString() : null);
    }

    public static Todo fromJson(JsonObject json) {
        Todo todo = new Todo();
        todo.setId(json.getLong("id"));
        todo.setTitle(json.getString("title"));
        todo.setDescription(json.getString("description"));
        todo.setCompleted(json.getBoolean("completed", false));
        todo.setCreatedAt(json.getString("createdAt") != null ? 
            LocalDateTime.parse(json.getString("createdAt")) : null);
        todo.setUpdatedAt(json.getString("updatedAt") != null ? 
            LocalDateTime.parse(json.getString("updatedAt")) : null);
        return todo;
    }
} 