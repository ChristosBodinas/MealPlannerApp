package org.example.mealplannerapp.repository;

import java.util.Optional;

import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DayRepository extends JpaRepository<Day, Long> {

    /**
     * Verifies that the {@link Day} with identifier {@code dayId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, fetches it from the database.
     *
     * @param userId
     * @param dayId
     * @return
     */
    @Query("SELECT d FROM Day d WHERE d.plan.user.id = :userId AND d.id = :dayId")
    Optional<Day> fetchByIdVerified(
        @Param("userId") Long userId,
        @Param("dayId") Long dayId
    );

    /**
     * Verifies that a {@link Day} with identifier {@code dayId} exists and is owned by the
     * {@link User} with identifier {@code userId}.
     *
     * @param userId the identifier of the day's owner
     * @param dayId the identifier of the requested day
     * @return true if the given day exists for the given user, or false otherwise
     */
    @Query("SELECT COUNT(d) > 0 FROM Day d WHERE d.plan.user.id = :userId AND d.id = :dayId")
    boolean existsByIdVerified(
        @Param("userId") Long userId,
        @Param("dayId") Long dayId
    );

}
