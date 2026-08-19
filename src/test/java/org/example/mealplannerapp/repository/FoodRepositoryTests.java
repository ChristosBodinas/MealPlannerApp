package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Exercise;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FoodRepositoryTests {

    // TODO: Review messages.

    // CONSTANTS
    private static final String MY_AUTH_ID = "alice_auth";
    private static final String MY_USERNAME = "Alice1";

    private static final String OTHER_AUTH_ID = "bob_auth";
    private static final String OTHER_USERNAME = "Bob2";

    // VARIABLES
    @Autowired private TestEntityManager entityManager;
    @Autowired private FoodRepository foodRepository;

    private User myUser;
    private User otherUser;

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void prepareOtherUser() {
        otherUser = defaultUser().authId(OTHER_AUTH_ID).username(OTHER_USERNAME).build();
        entityManager.persist(otherUser);
    }

    private Food prepareFood(User owner) {
        Food food = defaultFood().user(owner).build();
        entityManager.persist(food);
        return food;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareMyUser() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        entityManager.persist(myUser);
    }

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Given a valid foodId owned by the current user, " +
            "returns the Food and loads its associated units and prices.")
        void validId_fetchesFoodWithUnitsAndPrices() {
            // Arrange
            Food food = prepareFood(myUser);
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();

            Food fetched = result.get();

            assertThat(fetched.getId()).as("Fetched food should have the correct ID.")
                    .isEqualTo(food.getId());

            assertThat(fetched.getUser().getId()).as("Fetched food should belong to the current user.")
                    .isEqualTo(myUser.getId());

            assertThat(Hibernate.isInitialized(fetched.getUnits())).as("Reference units should be loaded.")
                    .isTrue();

            assertThat(Hibernate.isInitialized(fetched.getPrices())).as("Prices should be loaded.")
                    .isTrue();
        }

        @Test
        @DisplayName("Given an invalid foodId, returns empty.")
        void idNotFound_returnsEmpty() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given a non-owned foodId, returns empty.")
        void idNotOwned_returnsEmpty() {
            // Arrange
            prepareOtherUser();
            Food food = prepareFood(otherUser);
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

    }

    @Nested
    @DisplayName("fetchShallowByUserAndText")
    class FetchShallowByUserAndText {

        private Pageable pageable;

        private Food prepareFoodWithText(User owner, String name, String brand) {
            Food food = defaultFood().user(owner).name(name).brand(brand).build();
            entityManager.persist(food);
            return food;
        }

        @BeforeEach
        void preparePageable() {
            pageable = PageRequest.of(0, 2);
        }

        @Test
        @DisplayName("Given a non-empty text, only returns foods with at least a partial name match.")  // TODO: Rephrase.
        void nonEmptyText_returnsMatchingName() {
            // Arrange
            Food match = prepareFoodWithText(myUser, "_A_myText_B_", "generic");
            Food noMatch = prepareFoodWithText(myUser, "_A_nope_B_", "generic");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method should only return the food with the matching name.")
                    .extracting(Food::getId).containsExactly(match.getId());

        }

        @Test
        @DisplayName("Given a non-empty text, only returns foods with at least a partial brand match.")  // TODO: Rephrase.
        void nonEmptyText_returnsMatchingBrand() {
            // Arrange
            Food match = prepareFoodWithText(myUser, "generic", "_A_myText_B_");
            Food noMatch = prepareFoodWithText(myUser, "generic", "_A_nope_B_");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method should only return the food with the matching name.")
                    .extracting(Food::getId).containsExactly(match.getId());

        }

        @Test
        @DisplayName("Given a non-empty text, only returns matching exercises owned by the current user.")
        void nonEmptyText_returnsOnlyOwnedMatches() {
            // Arrange
            prepareOtherUser();
            Food owned = prepareFoodWithText(myUser, "_A_myText_B_", "_A_myText_B_");
            Food notOwned = prepareFoodWithText(otherUser, "A_myText_B_", "_A_myText_B_");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method should only return matching food owned by the current user.")
                    .extracting(Food::getId).containsExactly(owned.getId());
        }

        @Test
        @DisplayName("Does not load the associated units or prices of returned foods.")
        void doesNotLoadUnitsAndPrices() {
            // Arrange
            Food match = prepareFoodWithText(myUser, "_A_myText_B_", "_A_myText_B_");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method should return the exercise with the matching name.")
                    .extracting(Food::getId).containsExactly(match.getId());

            Food fetched = result.toList().getFirst();
            assertThat(Hibernate.isInitialized(fetched.getUnits())).as("Food units should not be loaded.")
                    .isFalse();
            assertThat(Hibernate.isInitialized(fetched.getPrices())).as("Food prices should not be loaded.")
                    .isFalse();
        }

        @Test
        @DisplayName("Given a null text, returns all foods owned by the current user.")
        void givenNullText_returnsAllOwnedExercises() {
            // Arrange
            Food owned1 = prepareFoodWithText(myUser, "a", "b");
            Food owned2 = prepareFoodWithText(myUser, "c", "d");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), null, pageable);

            // Assert
            assertThat(result).as("Method should return all owned foods.")
                    .extracting(Food::getId).containsExactlyInAnyOrder(owned1.getId(), owned2.getId());
        }

        @Test
        @DisplayName("Given an empty text, returns all foods owned by the current user.")
        void givenEmptyText_returnsAllOwnedExercises() {
            // Arrange
            Food owned1 = prepareFoodWithText(myUser, "a", "b");
            Food owned2 = prepareFoodWithText(myUser, "c", "d");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "", pageable);

            // Assert
            assertThat(result).as("Method should return all owned foods.")
                    .extracting(Food::getId).containsExactlyInAnyOrder(owned1.getId(), owned2.getId());
        }

    }

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {

        @Test
        @DisplayName("Given a valid foodId owned by the current user, " +
                "deletes the food and its units and prices, then returns 1.")
        void validId_deletesExerciseAndIntensityLevels() {
            // Arrange
            Food food = prepareFood(myUser);
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(foodRepository.existsById(food.getId()))
                    .as("Food should no longer exist.")
                    .isFalse();

            long unitsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).as("Associated units should no longer exist.").isZero();

            long pricesCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_price\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(pricesCount).as("Associated prices should no longer exist.").isZero();

            assertThat(result).as("Method output should be 1.").isOne();
        }

        @Test
        @DisplayName("Given a foodId that does not exist, returns 0.")
        void idNotFound_returnsZero() {
            // Arrange
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();
        }

        @Test
        @DisplayName("Given a foodId owned by another user, returns 0.")
        void idNotOwned_returnsZero() {
            // Arrange
            prepareOtherUser();
            Food food = prepareFood(otherUser);
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();

            assertThat(foodRepository.existsById(food.getId())).as("Food should still exist.")
                    .isTrue();

            long unitsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).as("Associated units should still exist.").isNotZero();

            long pricesCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"food_price\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(pricesCount).as("Associated prices should still exist.").isNotZero();
        }

    }
}
