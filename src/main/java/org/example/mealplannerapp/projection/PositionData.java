package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.constants.Category;

public interface PositionData {
    Long getDayId();
    Category getCategory();
    int getPosition();
}
