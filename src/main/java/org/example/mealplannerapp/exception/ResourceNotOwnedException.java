package org.example.mealplannerapp.exception;

public class ResourceNotOwnedException extends RuntimeException {
    public ResourceNotOwnedException(String message) {
        super(message);
    }
}
