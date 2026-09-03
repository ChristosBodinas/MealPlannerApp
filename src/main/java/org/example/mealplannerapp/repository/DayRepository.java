package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {

    @Query("SELECT d FROM Day d WHERE d.plan.user.id = :userId AND d.id = :dayId")
    Optional<Day> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("dayId") Long dayId
    );

    @Query("SELECT COUNT(d) > 0 FROM Day d WHERE d.plan.user.id = :userId AND d.id = :dayId")
    boolean existsByIdVerified(
            @Param("userId") Long userId,
            @Param("dayId") Long dayId
    );

}
