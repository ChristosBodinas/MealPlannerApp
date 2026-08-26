package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FoodRepositoryTests {

    // CONSTANTS
    private static final String MY_AUTH_ID = "MyAuthId";
    private static final String MY_USERNAME = "MyUsername";
    private static final String OTHER_AUTH_ID = "OtherAuthId";
    private static final String OTHER_USERNAME = "OtherUsername";

    // BEANS
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private FoodRepository foodRepository;

    // VARIABLES
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

    private Food prepareDefaultFood(User owner) {
        Food food = defaultFood().user(owner).build();
        entityManager.persist(food);
        return food;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareUser() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        entityManager.persist(myUser);
    }

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Given an existing foodId owned by the current user, " +
                "returns the food and loads its associated units and vendors.")
        void foodFetchedWithUnitsAndVendors() {
            // Arrange
            Food food = prepareDefaultFood(myUser);
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();

            Food fetched = result.get();
            assertThat(Hibernate.isInitialized(fetched.getUnits())).as("Reference units should be loaded.")
                    .isTrue();
            assertThat(Hibernate.isInitialized(fetched.getVendors())).as("Vendor data should be loaded.")
                    .isTrue();
        }

        @Test
        @DisplayName("Given a non-existent foodId, returns empty.")
        void foodNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing foodId owned by another user, returns empty.")
        void foodNotOwned() {
            // Arrange
            prepareOtherUser();
            Food food = prepareDefaultFood(otherUser);
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

        private Food prepareNamedBrandedFood(User owner, String name, String brand) {
            Food food = defaultFood().user(owner).name(name).brand(brand).build();
            entityManager.persist(food);
            return food;
        }

        @BeforeEach
        void preparePageable() {
            pageable = PageRequest.of(0, 2);
        }

        @Test
        @DisplayName("Given a non-empty string, returns only those foods with at least a partial name match.")
        void onlyMatchingNameFetched() {
            // Arrange
            Food match = prepareNamedBrandedFood(myUser, "_A_myText_B_", "generic");
            Food noMatch = prepareNamedBrandedFood(myUser, "_A_nope_B_", "generic");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain only the food with the matching name.")
                    .extracting(Food::getId).containsExactly(match.getId());
        }

        @Test
        @DisplayName("Given a non-empty string, returns only those foods with at least a partial brand match.")
        void onlyMatchingBrandFetched() {
            // Arrange
            Food match = prepareNamedBrandedFood(myUser, "generic", "_A_myText_B_");
            Food noMatch = prepareNamedBrandedFood(myUser, "generic", "_A_nope_B_");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain only the food with the matching brand.")
                    .extracting(Food::getId).containsExactly(match.getId());
        }

        @Test
        @DisplayName("Given a non-empty string, returns only those matching foods that belong to the current user.")
        void onlyOwnedMatchesFetched() {
            // Arrange
            Food owned = prepareNamedBrandedFood(myUser, "_A_myText_B", "generic");
            prepareOtherUser();
            Food notOwned = prepareNamedBrandedFood(otherUser, "_A_myText_B", "generic");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain only the food owned by the current user.")
                    .extracting(Food::getId).containsExactly(owned.getId());
        }

        @Test
        @DisplayName("Does not load the units and vendors of any returned foods.")
        void unitsAndVendorsNotLoaded() {
            // Arrange
            Food food = prepareNamedBrandedFood(myUser, "_A_myText_B_", "generic");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), "myText", pageable);

            // Assert
            assertThat(result).as("Method output should contain the food.")
                    .extracting(Food::getId).containsExactly(food.getId());

            Food fetched = result.toList().getFirst();
            assertThat(Hibernate.isInitialized(fetched.getUnits()))
                    .as("Reference units should not be loaded.")
                    .isFalse();
            assertThat(Hibernate.isInitialized(fetched.getVendors()))
                    .as("Vendor data should not be loaded.")
                    .isFalse();
        }

        @ParameterizedTest(name = "Given a {0} string, returns all foods owned by the current user.")
        @NullAndEmptySource
        @DisplayName("Given a null or empty string, returns all foods owned by the current user.")
        void nullOrEmptyTextFetchesAll(String text) {
            // Arrange
            Food one = prepareNamedBrandedFood(myUser, "a", "b");
            Food two = prepareNamedBrandedFood(myUser, "c", "d");
            flushAndClear();

            // Act
            Page<Food> result = foodRepository.fetchShallowByUserAndText(myUser.getId(), text, pageable);

            // Assert
            assertThat(result).as("Method output should return all food.")
                    .extracting(Food::getId).containsExactlyInAnyOrder(one.getId(), two.getId());
        }

    }

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {

        @Test
        @DisplayName("Given an existing foodId owned by the current user, " +
                "deletes the food and its associated units/vendors, then returns 1.")
        void foodAndUnitsAndVendorsDeleted() {
            // Arrange
            Food food = prepareDefaultFood(myUser);
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).as("Method output should be 1.").isOne();

            assertThat(foodRepository.existsById(food.getId()))
                    .as("Food should no longer exist.")
                    .isFalse();

            long unitsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"reference_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).as("Associated reference units should no longer exist.").isZero();

            long vendorsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"vendor_data\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(vendorsCount).as("Associated vendor data should no longer exist.").isZero();
        }

        @Test
        @DisplayName("Given a non-existent foodId, returns 0.")
        void foodNotFound() {
            // Arrange
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();
        }

        @Test
        @DisplayName("Given an existing foodId owned by another user, returns 0.")
        void foodNotOwned() {
            // Arrange
            prepareOtherUser();
            Food food = prepareDefaultFood(otherUser);
            flushAndClear();

            // Act
            int result = foodRepository.deleteByIdVerified(myUser.getId(), food.getId());

            // Assert
            assertThat(result).as("Method output should be 0.").isZero();

            assertThat(foodRepository.existsById(food.getId()))
                    .as("Food should still exist.")
                    .isTrue();

            long unitsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"reference_unit\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(unitsCount).as("Associated reference units should still exist.").isNotZero();

            long vendorsCount = (long) entityManager.getEntityManager()
                    .createNativeQuery("SELECT COUNT(*) FROM \"vendor_data\" WHERE \"food_id\" = :foodId")
                    .setParameter("foodId", food.getId())
                    .getSingleResult();
            assertThat(vendorsCount).as("Associated vendor data should still exist.").isNotZero();
        }

    }

}
