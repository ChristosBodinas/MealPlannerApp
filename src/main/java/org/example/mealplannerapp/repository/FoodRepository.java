package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface FoodRepository extends JpaRepository<Food, Long> {

    Set<Food> findAllByUserId(Long userId);

    @Query("SELECT f FROM Food f WHERE f.user.id = :userId " +
            "AND (LOWER(f.brand) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(f.name) LIKE LOWER(CONCAT('%', :text, '%')))")
    Set<Food> findAllByUserIdAndTextSearch(
            @Param("userId") Long userId,
            @Param("text") String text);

}
