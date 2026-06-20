package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findAllByUserId(Long userId);

    @NativeQuery("SELECT * FROM food WHERE user_id = :userId " +
                    "AND (LOWER(brand) LIKE LOWER(CONCAT('%', :text, '%')) " +
                    "OR LOWER(name) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Food> findAllByUserAndTextSearch(
            @Param("userId") Long userId,
            @Param("text") String text);

    // TO DO: Learn how to use @Query to make this name less unyieldy.
    List<Food> findAllByUserIdAndBrandContainingIgnoreCaseOrNameContainingIgnoreCase(Long userId, String brand, String name);

}
