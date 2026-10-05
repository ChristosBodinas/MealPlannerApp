package org.example.mealplannerapp.repository;


import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query("SELECT e FROM Entry e " +
            "LEFT JOIN FETCH TREAT(e AS FoodEntry).food f " +
            "LEFT JOIN FETCH f.units u LEFT JOIN FETCH f.vendors v " +
            "LEFT JOIN FETCH TREAT(e AS ExerciseEntry).exercise x " +
            "LEFT JOIN FETCH x.levels l " +
            "WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Entry> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    @Query("SELECT e FROM Entry e WHERE e.day.id = :dayId " +
            "ORDER BY e.category ASC, e.position ASC")
    List<Entry> fetchShallowByDayOrdered(
            @Param("dayId") Long dayId
    );

    @Query("SELECT new org.example.mealplannerapp.projection.Placement" +
            "(e.day.id, e.category, e.position) " +
            "FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Placement> extractPlacementByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    @Query("SELECT new org.example.mealplannerapp.projection.CategoryStats" +
            "(e.category, SUM(e.calories), SUM(e.protein), SUM(e.carbs), " +
            "SUM(e.fat), SUM(e.fiber), SUM(e.price)) " +
            "FROM Entry e WHERE e.day.id = :dayId " +
            "GROUP BY e.category")
    List<CategoryStats> summarizeCategoriesByDay(
            @Param("dayId") Long dayId
    );

    @Query("SELECT new org.example.mealplannerapp.projection.DayStats" +
            "(e.day.id, SUM(e.calories), SUM(e.protein), SUM(e.carbs), " +
            "SUM(e.fat), SUM(e.fiber), SUM(e.price)) " +
            "FROM Entry e WHERE e.day.plan.id = :planId " +
            "GROUP BY e.day")
    List<DayStats> summarizeDaysByPlan(
            @Param("planId") Long planId
    );

    @Query("SELECT new org.example.mealplannerapp.projection.ShoppingItem" +
            "(e.name, SUM(e.grams)) " +
            "FROM Entry e WHERE e.day.plan.id = :planId AND TYPE(e) = FoodEntry " +
            "GROUP BY e.name")
    List<ShopItem> extractShoppingListByPlan(
            @Param("planId") Long planId
    );

    @Query("SELECT COUNT(e) FROM Entry e WHERE e.day.id = :dayId AND e.category = :category")
    int countByDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category
    );

    @Modifying
    @Query("UPDATE Entry e SET e.position = e.position - 1 " +
            "WHERE e.day.id = :dayId AND e.category = :category " +
            "AND e.position > :emptySpot")
    void closeGapAfterRemoval(
            @Param("dayId") Long dayId,
            @Param("category") Category category,
            @Param("emptySpot") int emptySpot
    );

    @Modifying
    @Query("DELETE FROM Entry e WHERE e.day.id = :dayId")
    void deleteByDay(
            @Param("dayId") Long dayId
    );

    @Modifying
    @Query("DELETE FROM Entry e WHERE e.day.plan.id = :planId")
    void deleteByPlan(
            @Param("planId") Long planId
    );
}
