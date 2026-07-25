package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.projection.Placement;
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
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDayBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.DEFAULT_CATEGORY;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntryBuilder;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class EntryRepositoryTests {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User currentUser;
    private User otherUser;
    private Plan currentUserPlan;
    private Plan otherUserPlan;
    private Day currentUserDay;
    private Day otherUserDay;

    private final String CURRENT_USERNAME = "alice";
    private final String OTHER_USERNAME = "bob";

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @BeforeEach
    void prepareCurrentUser() {
        currentUser = defaultUserBuilder().username(CURRENT_USERNAME).build();
        currentUserPlan = defaultPlanBuilder().user(currentUser).build();
        currentUserDay = defaultDayBuilder().plan(currentUserPlan).build();
        currentUserPlan.getDays().add(currentUserDay);

        entityManager.persist(currentUser);
        entityManager.persist(currentUserPlan);
    }

    void prepareOtherUser() {
        otherUser = defaultUserBuilder().username(OTHER_USERNAME).build();
        otherUserPlan = defaultPlanBuilder().user(otherUser).build();
        otherUserDay = defaultDayBuilder().plan(otherUserPlan).build();
        otherUserPlan.getDays().add(otherUserDay);

        entityManager.persist(otherUser);
        entityManager.persist(otherUserPlan);
    }

    @Nested
    class fetchFoodEntryByIdVerified {

        @Test
        @DisplayName("Returns the requested FoodEntry, its referenced Food, and the Food's associated " +
                "units/price when the entry exists and belongs to the given user.")
        void entryFetched() {
            // Arrange
            Food food = defaultFoodBuilder().user(currentUser).build();
            FoodEntry entry = defaultFoodEntryBuilder().day(currentUserDay).food(food).build();
            entityManager.persist(food);
            entityManager.persist(entry);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchFoodEntryByIdVerified(currentUser.getId(), entry.getId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(entry.getId());

            Long ownerId = result.get().getDay().getPlan().getUser().getId();
            assertThat(ownerId).isEqualTo(currentUser.getId());

            FoodEntry castResult = (FoodEntry) result.get();
            assertThat(Hibernate.isInitialized(castResult.getFood())).isTrue();
            assertThat(Hibernate.isInitialized(castResult.getFood().getUnits())).isTrue();
            assertThat(Hibernate.isInitialized(castResult.getFood().getPrices())).isTrue();
        }

        @Test
        @DisplayName("Returns empty when the requested entry does not exist.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchFoodEntryByIdVerified(currentUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested entry exists but does not belong to the given user.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            Food food = defaultFoodBuilder().user(otherUser).build();
            FoodEntry entry = defaultFoodEntryBuilder().day(otherUserDay).food(food).build();
            entityManager.persist(food);
            entityManager.persist(entry);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchFoodEntryByIdVerified(currentUser.getId(), entry.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(entry.getId())).isTrue();
        }

    }

    @Nested
    class fetchPlacementByIdVerified {

        private FoodEntry entry;

        private final Category TEST_CATEGORY = Category.DINNER;
        private final int TEST_POSITION = 5;

        void prepareEntry(User owner, Day ownerDay) {
            Food food = defaultFoodBuilder().user(owner).build();
            entry = defaultFoodEntryBuilder()
                    .day(ownerDay)
                    .food(food)
                    .category(TEST_CATEGORY)
                    .position(TEST_POSITION)
                    .build();

            entityManager.persist(food);
            entityManager.persist(entry);
        }

        @Test
        @DisplayName("Returns the requested entry's placement data when the entry exists and belongs to the given user.")
        void placementFetched() {
            // Arrange
            prepareEntry(currentUser, currentUserDay);
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.fetchPlacementByIdVerified(currentUser.getId(), entry.getId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getDayId()).isSameAs(currentUserDay.getId());
            assertThat(result.get().getCategory()).isEqualTo(TEST_CATEGORY);
            assertThat(result.get().getPosition()).isEqualTo(TEST_POSITION);
        }

        @Test
        @DisplayName("Returns empty when the requested entry does not exist.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchFoodEntryByIdVerified(currentUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested entry exists but does not belong to the given user.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            prepareEntry(otherUser, otherUserDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchFoodEntryByIdVerified(currentUser.getId(), entry.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(entry.getId())).isTrue();
        }

    }

    @Nested
    class countInDayAndCategory {

        private final Category TEST_CATEGORY = Category.BREAKFAST;
        private final Category EXCLUDED_CATEGORY = Category.SNACK;

        FoodEntry prepareEntry(User owner, Day ownerDay) {
            Food food = defaultFoodBuilder().user(owner).build();
            FoodEntry entry = defaultFoodEntryBuilder()
                    .day(ownerDay)
                    .food(food)
                    .category(TEST_CATEGORY)
                    .build();

            entityManager.persist(food);
            entityManager.persist(entry);

            return entry;
        }

        @Test
        @DisplayName("Returns the number of entries in the given day and category.")
        void countEntries() {
            // Arrange
            FoodEntry entry1 = prepareEntry(currentUser, currentUserDay);
            FoodEntry entry2 = prepareEntry(currentUser, currentUserDay);
            FoodEntry entry3 = prepareEntry(currentUser, currentUserDay);
            flushAndClear();

            // Act
            long result = entryRepository.countInDayAndCategory(currentUserDay.getId(), TEST_CATEGORY);

            // Assert
            assertThat(result).isEqualTo(3);
        }

        @Test
        @DisplayName("Correctly excludes entries in the same category but a different day.")
        void excludeOtherDays() {
            // Arrange
            FoodEntry entry1 = prepareEntry(currentUser, currentUserDay);
            FoodEntry entry2 = prepareEntry(currentUser, currentUserDay);

            Day excludedDay = defaultDayBuilder().plan(currentUserPlan).build();
            currentUserPlan.getDays().add(excludedDay);
            entityManager.persist(currentUserPlan);

            FoodEntry entry3 = prepareEntry(currentUser, excludedDay);

            flushAndClear();

            // Act
            long result = entryRepository.countInDayAndCategory(currentUserDay.getId(), TEST_CATEGORY);

            // Assert
            assertThat(result).isEqualTo(2);
            assertThat(entryRepository.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Correctly excludes entries in the same day but a different category.")
        void excludeOtherCategories() {
            // Arrange
            FoodEntry entry1 = prepareEntry(currentUser, currentUserDay);
            FoodEntry entry2 = prepareEntry(currentUser, currentUserDay);
            FoodEntry entry3 = prepareEntry(currentUser, currentUserDay);
            entry3.setCategory(EXCLUDED_CATEGORY);
            flushAndClear();

            // Act
            long result = entryRepository.countInDayAndCategory(currentUserDay.getId(), TEST_CATEGORY);

            // Assert
            assertThat(result).isEqualTo(2);
            assertThat(entryRepository.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Returns 0 if no matching entries are found.")
        void noMatches() {
            // Arrange
            flushAndClear();

            // Act
            long result = entryRepository.countInDayAndCategory(currentUserDay.getId(), TEST_CATEGORY);

            // Assert
            assertThat(result).isEqualTo(0);
        }

        // TODO: polymorphic counting test

    }

    @Nested
    class shiftUpInDayAndCategory {
        // TODO: shiftUpInDayAndCategory tests
    }

    @Nested
    class shiftDownInDayAndCategory {
        // TODO: shiftDownInDayAndCategory tests
    }

    @Nested
    class deleteByIdVerified {

        @Test
        @DisplayName("Deletes the requested entry when it exists and belongs to the given user.")
        void entryDeleted() {
            // Arrange
            Food food = defaultFoodBuilder().user(currentUser).build();
            FoodEntry entry = defaultFoodEntryBuilder().day(currentUserDay).food(food).build();
            entityManager.persist(food);
            entityManager.persist(entry);
            flushAndClear();

            // Act
            int rowsAffected = entryRepository.deleteByIdVerified(currentUser.getId(), entry.getId());

            // Assert
            assertThat(rowsAffected).isOne();
            assertThat(entryRepository.existsById(entry.getId())).isFalse();
        }

        @Test
        @DisplayName("Does nothing when the requested entry does not exist.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            int rowsAffected = entryRepository.deleteByIdVerified(currentUser.getId(), 999L);

            // Assert
            assertThat(rowsAffected).isZero();
            assertThat(entryRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Does nothing when the requested entry exists but does not belong to the given user.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            Food food = defaultFoodBuilder().user(otherUser).build();
            FoodEntry entry = defaultFoodEntryBuilder().day(otherUserDay).food(food).build();
            entityManager.persist(food);
            entityManager.persist(entry);
            flushAndClear();

            // Act
            int rowsAffected = entryRepository.deleteByIdVerified(currentUser.getId(), entry.getId());

            // Assert
            assertThat(rowsAffected).isZero();
            assertThat(entryRepository.existsById(entry.getId())).isTrue();
        }

    }

}