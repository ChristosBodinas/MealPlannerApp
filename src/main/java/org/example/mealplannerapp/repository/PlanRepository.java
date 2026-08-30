package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    @Query("SELECT p FROM Plan p JOIN FETCH p.days d " +
            "WHERE p.user.id = :userId AND p.id = :planId")
    Optional<Plan> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("planId") Long planId
    );

    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND (:text IS NULL OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%')))")
    Page<Plan> fetchShallowByUserAndText(
            @Param("userId") Long userId,
            @Param("text") String text,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) > 0 FROM Plan p WHERE p.user.id = :userId AND p.id = :planId")
    boolean existsByIdVerified(
            @Param("userId") Long userId,
            @Param("planId") Long planId
    );
}
