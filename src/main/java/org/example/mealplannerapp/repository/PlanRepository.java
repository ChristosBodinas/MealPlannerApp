package org.example.mealplannerapp.repository;

import java.util.List;
import java.util.Optional;

import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    
    /**
     * Verifies that the {@link Plan} with identifier {@code planId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, fetches it and eagerly loads its associated days.
     * @param userId the identifier of the plan's owner
     * @param planId the identifier of the requested plan
     * @return the requested plan and its associated days, or empty if no such plan is owned by
     * the given user
     */
    @Query("SELECT p FROM Plan p " +
        "LEFT JOIN FETCH p.days d " +
        "WHERE p.user.id = :userId AND p.id = :planId")
    Optional<Plan> fetchByIdVerified(
        @Param("userId") Long userId,
        @Param("planId") Long planId
    );

    /**
     * Fetches every {@code Plan} that is owned by the {@link User} with identifier {@code userId}
     * whose name contains the given {@code text}. Case-insensitive. Does not fetch the associated
     * days.
     * @param userId the identifier of the plans' owner
     * @param text   the text to search for
     * @return a list of matching plans, or an empty list if none match
     */
    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND " +
        "LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Plan> fetchShallowByUserAndText(
        @Param("userId") Long userId,
        @Param("text") String text
    );

    // TODO: Javadoc.
    @Query("SELECT COUNT(p) > 0 FROM Plan p WHERE p.user.id = :userId AND p.id = :planId")
    boolean existsByIdVerified(
        @Param("userId") Long userId,
        @Param("planId") Long planId
    );

}
