package org.example.mealplannerapp.repository;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query("SELECT e FROM Entry e WHERE e.user.id = :userId AND e.id = :entryId")
    Optional<Entry> findByIdVerified(Long userId, Long entryId);

    @Query("DROP FROM Entry e WHERE e.user.id = :userId AND e.id = :entryId")
    int deleteByIdVerified(Long userId, Long entryId);

    @Query("SELECT e FROM Entry e WHERE e.day.id = :dayId")
    List<Entry> findAllInDay(Long dayId);

    @Query("DROP FROM Entry e WHERE e.day.id = :dayId")
    int deleteAllInDay(Long dayId);

    @Query("SELECT e FROM Entry e WHERE e.day.id = :dayId AND e.category = :category")
    List<Entry> findAllInDayAndCategory(Long dayId, Category category);
}