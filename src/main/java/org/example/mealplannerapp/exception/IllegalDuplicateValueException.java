package org.example.mealplannerapp.exception;

public class IllegalDuplicateValueException extends RuntimeException {
    public IllegalDuplicateValueException(String message) {
        super(message);
    }
}