package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {

}