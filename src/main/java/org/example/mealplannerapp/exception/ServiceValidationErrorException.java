package org.example.mealplannerapp.exception;

public class ServiceValidationErrorException extends RuntimeException {
    public ServiceValidationErrorException(String message) {
        super(message);
    }
}
