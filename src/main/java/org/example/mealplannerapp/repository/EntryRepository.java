package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.projection.PositionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query("SELECT e FROM Entry e " +
            "LEFT JOIN FETCH TREAT(e AS FoodEntry).food f " +
            "LEFT JOIN FETCH f.units u " +
            "LEFT JOIN FETCH f.prices p " +
            "WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Entry> findByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    @Query("SELECT e FROM Entry e " +
            "LEFT JOIN FETCH TREAT(e AS FoodEntry).food f " +
            "LEFT JOIN FETCH f.units u " +
            "LEFT JOIN FETCH f.prices p " +
            "WHERE e.day.id = :dayId " +
            "ORDER BY e.category ASC, e.position ASC")
    List<Entry> findAllInDayOrdered(
            @Param("dayId") Long dayId
    );

    @Query("SELECT e FROM Entry e WHERE e.day.plan.user.id = :userId AND e.day.id = :dayId AND e.id = :entryId")
    Optional<Entry> findShallowByIdAndDayVerified(
            @Param("userId") Long userId,
            @Param("dayId") Long dayId,
            @Param("entryId") Long entryId
    );

    @Query("SELECT e.day.id AS dayId, e.category AS category, e.position as position " +
            "FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<PositionData> findPositionDataByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    @Query("SELECT COUNT(e) FROM Entry e WHERE e.day.id = :dayId AND e.category = :category")
    long countInDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category
    );

    @Modifying
    @Query("UPDATE Entry e SET e.position = e.position + 1 " +
            "WHERE e.day.id = :dayId AND e.category = :category " +
            "AND (:minPosition IS NULL OR e.position >= :minPosition) " +
            "AND (:maxPosition IS NULL OR e.position < :maxPosition)")
    void shiftUpInDayAndCategory(
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
    void shiftDownInDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category,
            @Param("minPosition") Integer minPosition,
            @Param("maxPosition") Integer maxPosition
    );

    @Modifying
    @Query("DELETE FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    int deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    @Query("DELETE FROM Entry e WHERE e.day.id = :dayId")
    int deleteAllInDay(
            @Param("dayId") Long dayId
    );
}