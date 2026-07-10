package org.example.mealplannerapp.repository;

public interface DayRepository extends JpaRepository<Day, Long> {

    @Query("SELECT d FROM Day d WHERE d.user.id = :userId AND d.id = :dayId")    
    Optional<Day> findByIdVerified(Long userId, Long dayId);

}