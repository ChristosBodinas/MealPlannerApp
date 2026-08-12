package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    /**
     * Verifies that the {@link Exercise} with identifier {@code exerciseId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, fetches it and eagerly loads its associated intensity
     * levels.
     *
     * @param userId     the identifier of the exercise's owner
     * @param exerciseId the identifier of the requested exercise
     * @return the requested exercise with its associated intensity levels, or empty if no such exercise
     * is owned by the given user
     */
    @Query("SELECT x FROM Exercise x " +
            "LEFT JOIN FETCH x.levels l " +
            "WHERE x.user.id = :userId AND x.id = :exerciseId"
    )
    Optional<Exercise> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId
    );

    /**
     * Fetches every {@code Exercise} that is owned by the {@link User} with identifier {@code userId}
     * whose name contains the given {@code text}. Case-insensitive. Does not fetch the associated
     * intensity levels.
     *
     * @param userId the identifier of the exercises' owner
     * @param text   the text to search for
     * @return a list of matching exercises, or an empty list if none match
     */
    @Query("SELECT x FROM Exercise x WHERE x.user.id = :userId AND " +
            "LOWER(x.name) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Exercise> fetchShallowByUserAndText(
            @Param("userId") Long userId,
            @Param("text") String text
    );

    /**
     * Verifies that the {@link Exercise} with identifier {@code exerciseId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, deletes it from the database along with its associated
     * intensity levels.
     *
     * @param userId     the identifier of the exercise's owner
     * @param exerciseId the identifier of the exercise to be deleted
     * @return the number of rows deleted (0 if no matching exercise was found, 1 otherwise)
     */
    @Modifying
    @Query("DELETE FROM Exercise x WHERE x.user.id = :userId AND x.id = :exerciseId")
    int deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId
    );

}
