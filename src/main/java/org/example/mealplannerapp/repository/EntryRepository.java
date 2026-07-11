package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.entry.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    @Query("SELECT e FROM Entry e WHERE e.day.plan.user.id = :userId AND e.id = :entryId")
    Entry findByIdVerified(@Param("userId") Long userId, @Param("entryId") Long entryId);
}