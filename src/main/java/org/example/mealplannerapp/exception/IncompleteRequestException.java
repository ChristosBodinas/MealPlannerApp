package org.example.mealplannerapp.exception;

public class IncompleteRequestException extends RuntimeException {
    public IncompleteRequestException(String message) {
        super(message);
    }
}
