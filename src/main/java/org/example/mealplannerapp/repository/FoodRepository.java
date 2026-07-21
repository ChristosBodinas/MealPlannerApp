package org.example.mealplannerapp.repository;

import java.util.List;
import java.util.Optional;

import org.example.mealplannerapp.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("SELECT f FROM Food f " +
            "LEFT JOIN FETCH f.units u " +
            "LEFT JOIN FETCH f.prices p " +
            "WHERE f.user.id = :userId AND f.id = :foodId")
    Optional<Food> fetchByIdVerified(
        @Param("userId") Long userId,
        @Param("foodId") Long foodId
    );

    @Query("SELECT f FROM Food f WHERE f.user.id = :userId AND " +
            "(LOWER(f.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(f.brand) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Food> fetchShallowByTextVerified(
        @Param("userId") Long userId,
        @Param("text") String text
    );

    @Modifying
    @Query("DELETE FROM Food f WHERE f.user.id = :userId AND f.id = :foodId")
    int deleteByIdVerified(
        @Param("userId") Long userId,
        @Param("foodId") Long foodId
    );
}
