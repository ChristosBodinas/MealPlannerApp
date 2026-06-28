package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

}