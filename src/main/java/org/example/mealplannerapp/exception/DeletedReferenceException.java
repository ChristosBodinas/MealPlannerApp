package org.example.mealplannerapp.exception;

public class DeletedReferenceException extends RuntimeException {
    public DeletedReferenceException(String message) {
        super(message);
    }
}
