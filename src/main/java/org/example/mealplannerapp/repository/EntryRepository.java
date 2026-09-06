package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.projection.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT new org.example.mealplannerapp.projection.Placement(" +
            "e.day.id, e.category, e.position) " +
            "FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Placement> extractPlacementByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    @Query("SELECT COUNT(e) FROM Entry e " +
            "WHERE e.day.id = :dayId AND e.category = :category")
    int countByDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category")Category category
    );

    @Modifying
    @Query("UPDATE Entry e SET e.position = e.position + 1 " +
            "WHERE e.day.id = :dayId AND e.category = :category " +
            "AND (:minPosition IS NULL OR e.position >= :minPosition) " +
            "AND (:maxPosition IS NULL OR e.position < :maxPosition)")
    void shiftUpByDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category,
            @Param("minPosition") Integer minPosition,
            @Param("maxPosition") Integer maxPosition
    );

    @Modifying
    @Query("UPDATE Entry e SET e.position = e.position - 1 " +
            "WHERE e.day.id = :dayId AND e.category = :category " +
            "AND (:minPosition IS NULL OR e.position > :minPosition) " +
            "AND (:maxPosition IS NULL OR e.position <= :maxPosition)")
    void shiftDownByDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category,
            @Param("minPosition") Integer minPosition,
            @Param("maxPosition") Integer maxPosition
    );
}
