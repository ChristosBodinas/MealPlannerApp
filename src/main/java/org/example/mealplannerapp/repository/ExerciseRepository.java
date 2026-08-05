package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Query("SELECT e FROM Exercise e " +
            "LEFT JOIN FETCH e.intensities i " +
            "WHERE e.user.id = :userId AND e.id = :exerciseId")
    Optional<Exercise> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId
    );

    @Query("SELECT e FROM Exercise e WHERE e.user.id = :userId AND " +
    "LOWER(e.name) LIKE LOWER(CONCAT('%', :text, '%')")
    List<Exercise> fetchShallowByUserAndText(
            @Param("userId") Long userId,
            @Param("text") String text
    );

    @Modifying
    @Query("DELETE FROM Exercise e WHERE e.user.id = :userId AND e.id = :exerciseId")
    int deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId
    );
}
