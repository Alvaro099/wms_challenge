package io.tenoro.app.domain.exception;

public class IllegalTaskStateException extends RuntimeException {
    public IllegalTaskStateException(String message) {
        super(message);
    }
}
