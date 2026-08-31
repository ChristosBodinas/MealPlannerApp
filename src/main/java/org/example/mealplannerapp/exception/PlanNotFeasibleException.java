package org.example.mealplannerapp.exception;

public class PlanNotFeasibleException extends RuntimeException {
    public PlanNotFeasibleException(String message) {
        super(message);
    }
}
