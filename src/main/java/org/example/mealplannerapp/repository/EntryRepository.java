package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.projection.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    /**
     * Verifies that the {@link Entry} with identifier {@code entryId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, fetches it and eagerly loads the referenced food
     * and its associated units and prices.
     *
     * @param userId  the identifier of the entry's owner
     * @param entryId the identifier of the requested entry
     * @return the requested entry and the full data of the entity it references, or empty
     * if no such entry is owned by the given user
     */
    default Optional<Entry> fetchByIdVerified(Long userId, Long entryId) {
        Class<? extends Entry> type = extractTypeById(entryId).orElse(null);

        if (type == FoodEntry.class) {
            return fetchFoodEntryByIdVerified(userId, entryId);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Verifies that the {@link FoodEntry} with identifier {@code entryId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, fetches it and eagerly loads the referenced food and
     * its associated units and prices.
     *
     * @param userId  the identifier of the entry's owner
     * @param entryId the identifier of the requested entry
     * @return the requested entry, its referenced food, and the food's associated units/prices, or
     * empty if no such entry is owned by the given user
     */
    @Query("SELECT e FROM Entry e " +
            "LEFT JOIN FETCH TREAT(e AS FoodEntry).food f " +
            "LEFT JOIN FETCH f.units u " +
            "LEFT JOIN FETCH f.prices p " +
            "WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Entry> fetchFoodEntryByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    /**
     * Finds the type of the {@link Entry} entity with identifier {@code entryId}.
     *
     * @param entryId the identifier of the requested entry
     * @return the type of the requested entry, or empty if it does not exist
     */
    @Query("SELECT TYPE(e) FROM Entry e WHERE e.id = :entryId")
    Optional<Class<? extends Entry>> extractTypeById(
            @Param("entryId") Long entryId
    );

    // TODO: default List<Entry> fetchAllByDay
    default List<Entry> fetchAllByDay(Long dayId) {
        List<Entry> entries = new ArrayList<>();
        return entries;
    }

    // TODO: javadoc
        @Query("SELECT e FROM Entry e " +
            "LEFT JOIN FETCH TREAT(e AS FoodEntry).food f " +
            "LEFT JOIN FETCH f.units u " +
            "LEFT JOIN FETCH f.prices p " +
            "WHERE e.day.id = :dayId")
    List<Entry> fetchAllFoodEntriesByDay(
        @Param("dayId") Long dayId
    );

    /**
     * Verifies that the {@link Entry} with identifier {@code entryId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, fetches information about its placement.
     *
     * @param userId  the identifier of the entry's owner
     * @param entryId the identifier of the requested entry
     * @return the dayId, category, and position of the requested entry
     */
    @Query("SELECT e.day.id AS dayId, e.category AS category, e.position AS position " +
            "FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Placement> extractPlacementByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    // TODO: extractCategoryTotalsByDay

    /**
     * Counts the total number of {@link Entry} entities that belong to the {@link Day} with
     * identifier {@code dayId} and the given {@code category}.
     *
     * @param dayId    the identifier of the target day
     * @param category the target category
     * @return the number of entries in the given day and category
     */
    @Query("SELECT COUNT(e) FROM Entry e WHERE e.day.id = :dayId AND e.category = :category")
    int countByDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category
    );

    /**
     * Increases the position of all {@link Entry} entities in the {@link Day} with identifier
     * {@code dayId} and in the given {@code category} by 1, provided that their current position
     * is between {@code minPosition} (inclusive) and {@code maxPosition} (exclusive).
     *
     * @param dayId       the identifier of the target day
     * @param category    the target category
     * @param minPosition the inclusive minimum position that can be affected,
     *                    or {@code null} if there is no lower bound
     * @param maxPosition the exclusive maximum position that can be affected,
     *                    or {@code null} if there is no upper bound
     */
    @Modifying
    @Query("UPDATE Entry e SET e.position = e.position + 1 " +
            "WHERE e.day.id = :dayId AND e.category = :category " +
            "AND (:minPosition IS NULL OR e.position >= :minPosition) " +
            "AND (:maxPosition IS NULL OR e.position < :maxPosition)")
    int shiftUpByDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category,
            @Param("minPosition") Integer minPosition,
            @Param("maxPosition") Integer maxPosition
    );

    /**
     * Decreases the position of all {@link Entry} entities in the {@link Day} with identifier
     * {@code dayId} and in the given {@code category} by 1, provided that their current position
     * is between {@code minPosition} (exclusive) and {@code maxPosition} (inclusive).
     *
     * @param dayId       the identifier of the target day
     * @param category    the target category
     * @param minPosition the exclusive minimum position that can be affected,
     *                    or {@code null} if there is no lower bound
     * @param maxPosition the inclusive maximum position that can be affected,
     *                    or {@code null} if there is no upper bound
     */
    @Modifying
    @Query("UPDATE Entry e SET e.position = e.position - 1 " +
            "WHERE e.day.id = :dayId AND e.category = :category " +
            "AND (:minPosition IS NULL OR e.position > :minPosition) " +
            "AND (:maxPosition IS NULL OR e.position <= :maxPosition)")
    int shiftDownByDayAndCategory(
            @Param("dayId") Long dayId,
            @Param("category") Category category,
            @Param("minPosition") Integer minPosition,
            @Param("maxPosition") Integer maxPosition
    );

    /**
     * Verifies that the {@link Entry} with identifier {@code entryId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, deletes it from the database.
     *
     * @param userId  the identifier of the entry's owner
     * @param entryId the identifier of the entry to be deleted
     * @return the number of rows deleted (0 if no matching entry was found, 1 otherwise)
     */
    @Modifying
    @Query("DELETE FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    int deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

    /**
     * Deletes all {@link Entry} entities owned by the {@link Day} with identifier {@code dayId}.
     * @param dayId the identifier of the entries' owning day
     */
    @Modifying
    @Query("DELETE FROM Entry e WHERE e.day.id = :dayId")
    void deleteAllByDay(
        @Param("dayId") Long dayId
    );

}