package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.entries.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
}
