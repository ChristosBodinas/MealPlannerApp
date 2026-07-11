package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.entry.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    // TO DO: Possibly tidy up and rename for consistency.

    @Query("SELECT e FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Optional<Entry> findByIdVerified(@Param("userId") Long userId, @Param("entryId") Long entryId);

    @Query("SELECT COUNT(e) FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id IN :entryIds")
    Long multipleIdsExistVerified(@Param("userId") Long userId, @Param("entryIds") Set<Long> entryIds);

    @Query("SELECT e FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id IN :entryIds")
    List<Entry> findMultipleByIdVerified(@Param("userId") Long userId, @Param("entryIds") Set<Long> entryIds);

    // PLACEHOLDER
    @Modifying
    @Query("DELETE FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id IN :entryIds")
    int deleteMultipleByIdVerified(@Param("userId") Long userId, @Param("entryIds") Set<Long> entryIds);
}