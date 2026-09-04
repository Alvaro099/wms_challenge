package io.tenoro.app.domain.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String id) {
        super("Replenishment task not found with ID: " + id);
    }
}
