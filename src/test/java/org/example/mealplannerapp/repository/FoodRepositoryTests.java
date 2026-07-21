package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.UserTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FoodRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FoodRepository foodRepository;

    private User owner;
    private User stranger;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @BeforeEach
    void prepareAllTests() {
        owner = defaultUserBuilder().username("William59").build();
        stranger = defaultUserBuilder().username("Jack37").build();

        entityManager.persist(owner);
        entityManager.persist(stranger);
    }

    @Nested
    class fetchByIdVerified {

        @Test
        @DisplayName("Given a valid userId and foodId, returns the Food and its associated units/prices.")
        void foodFetched() {
            // Arrange
            Food food = defaultFoodBuilder().user(owner).build();
            entityManager.persist(food);
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(owner.getId(), food.getId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(food.getId());
            assertThat(result.get().getUnits()).isNotEmpty();
            assertThat(result.get().getPrices()).isNotEmpty();
        }

        @Test
        @DisplayName("Given an invalid foodId, returns empty.")
        void foodNotFound() {
            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(owner.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Given a valid foodId but the wrong userId, returns empty.")
        void foodNotOwned() {
            // Arrange
            Food food = defaultFoodBuilder().user(owner).build();
            entityManager.persist(food);
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(stranger.getId(), food.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(foodRepository.findById(food.getId())).isPresent();
        }

    }

    @Nested
    class fetchShallowByTextVerified {

    }

    @Nested
    class deleteByIdVerified {

        @Test
        @DisplayName("Given a valid userId and foodId, deletes the Food and its associated units/prices.")
        void foodDeleted() {
            // Arrange
            Food food = defaultFoodBuilder().user(owner).build();
            entityManager.persist(food);
            flushAndClear();

            // Act
            int rowsDeleted = foodRepository.deleteByIdVerified(owner.getId(), food.getId());

            // Assert
            assertThat(rowsDeleted).isEqualTo(1);
            assertThat(foodRepository.findById(food.getId())).isEmpty();

            Long unitsCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).isEqualTo(0);

            Long pricesCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_price\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(pricesCount).isEqualTo(0);
        }

        @Test
        @DisplayName("Given an invalid foodId, deletes no rows.")
        void foodNotFound() {
            // Act
            int result = foodRepository.deleteByIdVerified(owner.getId(), 999L);

            // Assert
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Given a valid foodId but the wrong userId, deletes no rows.")
        void foodNotOwned() {
            // Arrange
            Food food = defaultFoodBuilder().user(owner).build();
            entityManager.persist(food);
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(stranger.getId(), food.getId());

            // Assert
            assertThat(result).isEqualTo(0);
            assertThat(foodRepository.findById(food.getId())).isPresent();

            Long unitsCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).isNotEqualTo(0);

            Long pricesCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_price\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(pricesCount).isNotEqualTo(0);
        }

    }

}