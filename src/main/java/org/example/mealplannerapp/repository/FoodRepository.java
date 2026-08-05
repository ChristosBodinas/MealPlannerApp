package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * Verifies that the {@link Food} with identifier {@code foodId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, fetches it and eagerly loads its associated units
     * and prices.
     *
     * @param userId the identifier of the food's owner
     * @param foodId the identifier of the requested food
     * @return the requested food with its associated units and prices, or empty if no such food
     * is owned by the given user
     */
    @Query("SELECT f FROM Food f " +
            "LEFT JOIN FETCH f.units u " +
            "LEFT JOIN FETCH f.prices p " +
            "WHERE f.user.id = :userId AND f.id = :foodId")
    Optional<Food> fetchByIdVerified(
            @Param("userId") Long userId,
            @Param("foodId") Long foodId
    );

        // TODO: Repository methods that return multiple entities should either all have "All" or none should.

    /**
     * Fetches every {@code Food} that is owned by the {@link User} with identifier {@code userId}
     * whose name or brand contains the given {@code text}. Case-insensitive. Does not fetch the
     * associated units and prices.
     *
     * @param userId the identifier of the foods' owner
     * @param text   the text to search for
     * @return a list of matching foods, or an empty list if none match
     */
    @Query("SELECT f FROM Food f WHERE f.user.id = :userId AND " +
            "(LOWER(f.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(f.brand) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Food> fetchShallowByUserAndText(
            @Param("userId") Long userId,
            @Param("text") String text
    );

    /**
     * Verifies that the {@link Food} with identifier {@code foodId} is owned by the {@link User}
     * with identifier {@code userId}, and if so, deletes it from the database along with its
     * associated units and prices.
     *
     * @param userId the identifier of the food's owner
     * @param foodId the identifier of the food to be deleted
     * @return the number of rows deleted (0 if no matching food was found, 1 otherwise)
     */
    @Modifying
    @Query("DELETE FROM Food f WHERE f.user.id = :userId AND f.id = :foodId")
    int deleteByIdVerified(
            @Param("userId") Long userId,
            @Param("foodId") Long foodId
    );
}
