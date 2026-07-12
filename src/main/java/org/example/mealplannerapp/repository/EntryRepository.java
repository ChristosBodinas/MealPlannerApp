package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.entity.entry.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query("SELECT e FROM Entry e " +
            "LEFT JOIN FETCH TREAT(e AS FoodEntry).food f " +
            "LEFT JOIN FETCH f.units " +
            "u LEFT JOIN FETCH f.prices p " +
            "WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Entry> findByIdVerified(@Param("userId") Long userId, @Param("entryId") Long entryId);

    @Query("SELECT COUNT(e) FROM Entry e WHERE e.day.id = :dayId AND e.category = :category")
    Long countInDayAndCategory(@Param("dayId") Long dayId, @Param("category") Category category);

    @Modifying
    @Query("DELETE FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    int deleteByIdVerified(@Param("userId") Long userId, @Param("entryId") Long entryId);
}