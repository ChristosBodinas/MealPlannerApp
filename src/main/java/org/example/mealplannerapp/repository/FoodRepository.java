package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("SELECT f FROM Food f " + 
        "LEFT JOIN FETCH f.units u " +
        "LEFT JOIN FETCH f.prices p " +
        "WHERE f.user.id = :userId AND f.id = :foodId")
    Optional<Food> findByIdVerified(
            @Param("userId") Long userId,
            @Param("foodId") Long foodId
    );

    @Modifying
    @Query("DELETE FROM Food f WHERE f.user.id = :userId AND f.id = :foodId")
    void deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("foodId") Long foodId
    );

    @Query("SELECT f FROM Food f WHERE f.user.id = :userId AND " +
        "(LOWER(f.name) LIKE CONCAT('%', :search, '%') OR " +
        "LOWER(f.brand) LIKE CONCAT('%', :search, '%'))")
    List<Food> searchByText(
            @Param("userId") Long userId,
            @Param("search") String search
    );
}
