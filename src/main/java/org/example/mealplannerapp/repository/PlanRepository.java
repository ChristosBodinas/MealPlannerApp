package org.example.mealplannerapp.repository;

import java.util.List;
import java.util.Optional;

import org.example.mealplannerapp.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    // TODO: Javadocs and general refinement.

    @Query("SELECT p FROM Plan p LEFT JOIN FETCH p.days d " +
            "WHERE p.user.id = :userId AND p.id = :planId")
    Optional<Plan> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("planId") Long planId
    );

    @Query("SELECT COUNT(p) > 0 FROM Plan p WHERE p.user.id = :userId AND p.id = :planId")
    boolean existsByIdVerified(
            @Param("userId") Long userId,
            @Param("planId") Long planId
    );

    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId")
    List<Plan> fetchAllShallowByUser(
            @Param("userId") Long userId
    );

}