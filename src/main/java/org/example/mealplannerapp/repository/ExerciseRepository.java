package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Query("SELECT x FROM Exercise x " +
            "LEFT JOIN FETCH x.levels l " +
            "WHERE x.user.id = :userId AND x.id = :exerciseId")
    Optional<Exercise> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId
    );

    @Query("SELECT x FROM Exercise x WHERE x.user.id = :userId AND (:text IS NULL OR " +
            "LOWER(x.name) LIKE LOWER(CONCAT('%', :text, '%')))")
    Page<Exercise> fetchShallowByUserAndText(
            @Param("userId") Long userId,
            @Param("text") String text,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM Exercise x WHERE x.user.id = :userId AND x.id = :exerciseId")
    int deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId
    );
}
