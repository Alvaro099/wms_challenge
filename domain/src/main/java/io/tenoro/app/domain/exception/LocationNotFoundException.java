package io.tenoro.app.domain.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String locationCode) {
        super("Location not found: " + locationCode);
    }
}
