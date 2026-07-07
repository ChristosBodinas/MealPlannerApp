package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("SELECT f FROM Food f " + 
        "JOIN FETCH f.units u " + 
        "JOIN FETCH f.prices p " + 
        "WHERE f.user.id = :userId AND f.id = :foodId")
    Food findByIdVerified(Long userId, Long foodId);

    @Query("DELETE FROM Food f WHERE f.user.id = :userId AND f.id = :foodID")
    Food deleteByIdVerified(Long userId, Long foodId);

    @Query("SELECT f FROM Food f WHERE f.user.id = :userId AND " +
        "(LOWER(f.name) LIKE CONCAT('%', :search, '%') OR" +
        "LOWER(f.brand) LIKE CONCAT('%', :search, '%))")
    List<Food> searchByText(Long userId, String search);
}
