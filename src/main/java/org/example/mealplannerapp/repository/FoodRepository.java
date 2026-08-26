package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("SELECT f FROM Food f " +
            "LEFT JOIN FETCH f.units u LEFT JOIN FETCH f.vendors v " +
            "WHERE f.user.id = :userId AND f.id = :foodId")
    Optional<Food> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("foodId") Long foodId
    );

    @Query("SELECT f FROM Food f WHERE f.user.id = :userId AND (:text IS NULL OR " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(f.brand) LIKE LOWER(CONCAT('%', :text, '%')))")
    Page<Food> fetchShallowByUserAndText(
            @Param("userId") Long userId,
            @Param("text") String text,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM Food f WHERE f.user.id = :userId AND f.id = :foodId")
    int deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("foodId") Long foodId
    );
}
