package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FoodRepositoryTests {

    // BEANS
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FoodRepository foodRepository;

    // VARIABLES
    private User myUser;
    private User otherUser;

    // CONSTANTS
    private final String MY_USERNAME = "alice1";
    private final String OTHER_USERNAME = "bob2";

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void prepareMyUser() {
        myUser = defaultUserBuilder().username(MY_USERNAME).build();
        entityManager.persist(myUser);
    }

    private void prepareOtherUser() {
        otherUser = defaultUserBuilder().username(OTHER_USERNAME).build();
        entityManager.persist(otherUser);
    }

    private Food prepareFood(User owner) {
        Food food = defaultFoodBuilder().user(owner).build();
        entityManager.persist(food);
        return food;
    }

    // TESTS PROPER

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Returns the requested food and eagerly loads its units/prices when it exists and belongs to the given user.")
        void foodFetched() {
            // Arrange
            Food food = prepareFood(myUser);
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).isPresent();
            Food fetched = result.get();

            assertThat(fetched.getId()).isEqualTo(food.getId());
            assertThat(fetched.getUser().getId()).isEqualTo(myUser.getId());
            assertThat(Hibernate.isInitialized(fetched.getUnits())).isTrue();
            assertThat(Hibernate.isInitialized(fetched.getPrices())).isTrue();
        }

        @Test
        @DisplayName("Returns empty when the requested food does not exist.")
        void foodNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(foodRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested food exists but belongs to a different user.")
        void foodNotOwned() {
            // Arrange
            prepareOtherUser();
            Food food = prepareFood(otherUser);
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(foodRepository.existsById(food.getId())).isTrue();
        }

    }
    
    
    @Nested
    @DisplayName("fetchShallowByUserAndText")
    class FetchShallowByUserAndText {

        private Food prepareFoodWithText(User owner, String name, String brand) {
            Food food = defaultFoodBuilder().user(owner).name(name).brand(brand).build();
            entityManager.persist(food);
            return food;
        }

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
            prepareOtherUser();
        }

        @Test
        @DisplayName("Returns owned foods with at least a partial name match when given a non-empty string.")
        void ownedFoodNameMatches() {
            // Arrange
            Food match = prepareFoodWithText(myUser, "black beans", "generic");
            Food noMatch = prepareFoodWithText(myUser, "black buns", "generic");
            Food notOwned = prepareFoodWithText(otherUser, "white beans", "generic");
            flushAndClear();

            // Act
            List<Food> results = foodRepository .fetchShallowByUserAndText(myUser.getId(), "bean");

            // Assert
            assertThat(results).extracting(Food::getId).containsExactly(match.getId());
            Food fetched = results.get(0);

            assertThat(Hibernate.isInitialized(fetched.getUnits())).isFalse();
            assertThat(Hibernate.isInitialized(fetched.getPrices())).isFalse();
        }

        @Test
        @DisplayName("Returns owned food with at least a partial brand match when given a non-empty string.")
        void ownedFoodBrandMatches() {
            // Arrange
            Food match = prepareFoodWithText(myUser, "generic", "black beans");
            Food noMatch = prepareFoodWithText(myUser, "generic", "black buns");
            Food notOwned = prepareFoodWithText(otherUser, "generic", "white beans");
            flushAndClear();

            // Act
            List<Food> results = foodRepository.fetchShallowByUserAndText(myUser.getId(), "bean");

            // Assert
            assertThat(results).extracting(Food::getId).containsExactly(match.getId());
            Food fetched = results.get(0);

            assertThat(Hibernate.isInitialized(fetched.getUnits())).isFalse();
            assertThat(Hibernate.isInitialized(fetched.getPrices())).isFalse();
        }

        @Test
        @DisplayName("Returns all owned foods when given an empty string.")
        void ownedFoodEmptyString() {
            // Arrange
            Food owned1 = prepareFoodWithText(myUser, "a", "b");
            Food owned2 = prepareFoodWithText(myUser, "c", "d");
            Food notOwned = prepareFoodWithText(otherUser, "e", "f");
            flushAndClear();

            // Act
            List<Food> results = foodRepository.fetchShallowByUserAndText(myUser.getId(), "");

            // Assert
            assertThat(results).extracting(Food::getId).containsExactlyInAnyOrder(owned1.getId(), owned2.getId());
            assertThat(Hibernate.isInitialized(results.get(0).getUnits())).isFalse();
            assertThat(Hibernate.isInitialized(results.get(0).getPrices())).isFalse();
            assertThat(Hibernate.isInitialized(results.get(1).getUnits())).isFalse();
            assertThat(Hibernate.isInitialized(results.get(1).getPrices())).isFalse();
        }

    }

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Deletes the requested food and its associated units/prices when it exists and belongs to the given user.")
        void foodDeleted() {
            // Arrange
            Food food = prepareFood(myUser);
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).isOne();
            assertThat(foodRepository.existsById(food.getId())).isFalse();

            Long unitsCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).isZero();

            Long pricesCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_price\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(pricesCount).isZero();
        }

        @Test
        @DisplayName("Does nothing when the requested food does not exist.")
        void foodNotFound() {
            // Arrange
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isZero();
            assertThat(foodRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Does nothing when the requested food exists but belongs to a different user.")
        void foodNotOwned() {
            // Arrange
            prepareOtherUser();
            Food food = prepareFood(otherUser);
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).isZero();
            assertThat(foodRepository.existsById(food.getId())).isTrue();

            Long unitsCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).isNotZero();

            Long pricesCount = (Long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_price\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(pricesCount).isNotZero();
        }

    }

}