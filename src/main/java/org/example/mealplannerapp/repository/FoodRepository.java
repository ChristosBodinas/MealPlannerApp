package org.example.mealplannerapp.repository;

import java.util.List;
import java.util.Optional;

import org.example.mealplannerapp.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long>{
    
    Optional<Food> fetchByIdVerified();

    // TODO: Replace with pages?
    List<Food> fetchShallowByUserAndText();

    int deleteByIdVerified();
}
