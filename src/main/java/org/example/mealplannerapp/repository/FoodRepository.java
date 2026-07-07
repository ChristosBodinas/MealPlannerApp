package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {
}
