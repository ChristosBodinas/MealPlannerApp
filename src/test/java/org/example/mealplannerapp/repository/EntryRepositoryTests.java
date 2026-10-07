package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.*;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.DayStats;
import org.example.mealplannerapp.projection.Placement;
import org.example.mealplannerapp.projection.ShopItem;
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
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDay;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultExerciseEntry;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntry;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExercise;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlan;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class EntryRepositoryTests {

    // CONSTANTS
    private static final String MY_AUTH_ID = "MyAuthId";
    private static final String MY_USERNAME = "MyUsername";
    private static final String OTHER_AUTH_ID = "OtherAuthId";
    private static final String OTHER_USERNAME = "OtherUsername";

    // BEANS
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private EntryRepository entryRepository;

    // VARIABLES
    private User myUser;
    private Plan myPlan;
    private Day myDay;
    private User otherUser;
    private Plan otherPlan;
    private Day otherDay;

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void prepareOtherUserPlanAndDay() {
        otherUser = defaultUser().authId(OTHER_AUTH_ID).username(OTHER_USERNAME).build();
        otherPlan = defaultPlan().user(otherUser).build();
        otherDay = defaultDay().plan(otherPlan).position(1).build();
        otherPlan.getDays().add(otherDay);

        entityManager.persist(otherUser);
        entityManager.persist(otherPlan);
    }

    private FoodEntry prepareFoodEntry(User owner, Day day) {
        Food food = defaultFood().user(owner).build();
        FoodEntry entry = defaultFoodEntry().day(day).food(food).build();
        entry.snapshotInfo();

        entityManager.persist(food);
        entityManager.persist(entry);

        return entry;
    }

    private ExerciseEntry prepareExerciseEntry(User owner, Day day) {
        Exercise exercise = defaultExercise().user(owner).build();
        ExerciseEntry entry = defaultExerciseEntry().day(day).exercise(exercise).build();
        entry.snapshotInfo();

        entityManager.persist(exercise);
        entityManager.persist(entry);

        return entry;
    }

    /**
     * Used to prepare an Entry for tests that don't care about the subtype but DO care about the
     * category and position.
     */
    private FoodEntry prepareEntryPositional(User owner, Day day, Category category, int position) {
        Food food = defaultFood().user(owner).build();
        FoodEntry entry = defaultFoodEntry()
                .day(day).category(category).position(position)
                .food(food)
                .build();

        entry.snapshotInfo();

        entityManager.persist(food);
        entityManager.persist(entry);

        return entry;
    }

    private void setEntryStats(Entry entry, int calories, int protein, int carbs, int fat, int fiber, int price) {
        entry.setCalories(BigDecimal.valueOf(calories));
        entry.setProtein(BigDecimal.valueOf(protein));
        entry.setCarbs(BigDecimal.valueOf(carbs));
        entry.setFat(BigDecimal.valueOf(fat));
        entry.setFiber(BigDecimal.valueOf(fiber));
        entry.setPrice(BigDecimal.valueOf(price));
    }

    private Plan prepareAdditionalPlan(User owner) {
        Plan plan = defaultPlan().user(owner).build();
        Day day = defaultDay().plan(plan).position(1).build();
        plan.getDays().add(day);
        entityManager.persist(plan);
        return plan;
    }

    private Day prepareAdditionalDay(Plan plan) {
        int position = plan.getDays().size() + 1;
        Day day = defaultDay().plan(plan).position(position).build();
        plan.getDays().add(day);

        // This method is always called after the parent Plan has been persisted.
        // Without an explicit persist() for the new day, it will remain transient until flush().
        entityManager.persist(day);

        return day;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareUserPlanAndDay() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        myPlan = defaultPlan().user(myUser).build();
        myDay = defaultDay().plan(myPlan).position(1).build();
        myPlan.getDays().add(myDay);

        entityManager.persist(myUser);
        entityManager.persist(myPlan);
    }

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Given the entryId of an existing FoodEntry owned by the current user, returns the entry " +
                "and loads the referenced food and its associated units and vendors.")
        void foodEntryFetched() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();
            FoodEntry fetched = (FoodEntry) result.get();

            assertThat(Hibernate.isInitialized(fetched.getFood()))
                    .as("Referenced food should be loaded.").isTrue();

            assertThat(Hibernate.isInitialized(fetched.getFood().getUnits()))
                    .as("Referenced food's units should be loaded.").isTrue();

            assertThat(Hibernate.isInitialized(fetched.getFood().getVendors()))
                    .as("Referenced food's vendors should be loaded.").isTrue();
        }

        @Test
        @DisplayName("Given the entryId of an existing ExerciseEntry owned by the current user, returns the entry " +
                "and loads the referenced exercise and its associated levels.")
        void exerciseEntryFetched() {
            // Arrange
            ExerciseEntry entry = prepareExerciseEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();
            ExerciseEntry fetched = (ExerciseEntry) result.get();

            assertThat(Hibernate.isInitialized(fetched.getExercise()))
                    .as("Referenced exercise should be loaded.").isTrue();

            assertThat(Hibernate.isInitialized(fetched.getExercise().getLevels()))
                    .as("Referenced exercise's levels should be loaded.").isTrue();
        }

        @Test
        @DisplayName("Given a non-existent entryId, returns empty.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing entryId owned by another user, returns empty.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUserPlanAndDay();
            ExerciseEntry entry = prepareExerciseEntry(otherUser, otherDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchShallowByDayOrdered")
    class FetchShallowByDayOrdered {

        @Test
        @DisplayName("Only fetches entries that belong to the day identified by dayId.")
        void correctEntriesFetched() {
            // Arrange
            Day invalidDay = prepareAdditionalDay(myPlan);
            FoodEntry validEntry = prepareFoodEntry(myUser, myDay);
            FoodEntry invalidEntry = prepareFoodEntry(myUser, invalidDay);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchShallowByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).as("Only entries from the specified day should be fetched.")
                    .extracting(Entry::getId)
                    .containsExactly(validEntry.getId());
        }

        @Test
        @DisplayName("Properly orders fetched entries by category and then by position within that category.")
        void entriesFetchedInOrder() {
            // Arrange
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            FoodEntry entry2 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 2);
            FoodEntry entry3 = prepareEntryPositional(myUser, myDay, Category.DINNER, 1);
            FoodEntry entry4 = prepareEntryPositional(myUser, myDay, Category.DINNER, 2);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchShallowByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).as("Fetched entries should be ordered by category and position.")
                    .extracting(Entry::getId)
                    .containsExactly(entry1.getId(), entry2.getId(), entry3.getId(), entry4.getId());
        }

        @Test
        @DisplayName("Does not load the foods referenced by the fetched entries.")
        void foodsNotLoaded() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchShallowByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).as("Method should fetch the food entry.")
                    .extracting(Entry::getId)
                    .containsExactly(entry.getId());

            FoodEntry fetched = (FoodEntry) result.getFirst();
            assertThat(Hibernate.isInitialized(fetched.getFood()))
                    .as("Referenced food should not be loaded.")
                    .isFalse();
        }

        @Test
        @DisplayName("Does not load the exercises referenced by the fetched entries.")
        void exercisesNotLoaded() {
            // Arrange
            ExerciseEntry entry = prepareExerciseEntry(myUser, myDay);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchShallowByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).as("Method should fetch the exercise entry.")
                    .extracting(Entry::getId)
                    .containsExactly(entry.getId());

            ExerciseEntry fetched = (ExerciseEntry) result.getFirst();
            assertThat(Hibernate.isInitialized(fetched.getExercise()))
                    .as("Referenced exercise should not be loaded.")
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("extractPlacementByIdVerified")
    class ExtractPlacementByIdVerified {

        @Test
        @DisplayName("Given an existing entryId owned by the current user, returns the entry's placement data.")
        void placementExtracted() {
            // Arrange
            ExerciseEntry entry = prepareExerciseEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();
            Placement extracted = result.get();

            assertThat(extracted).as("Placement contains the correct values.")
                    .extracting(Placement::dayId, Placement::category, Placement::position)
                    .containsExactly(myDay.getId(), entry.getCategory(), entry.getPosition());
        }

        @Test
        @DisplayName("Given a non-existent entryId, returns empty.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing entryId owned by another user, returns empty.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUserPlanAndDay();
            FoodEntry entry = prepareFoodEntry(otherUser, otherDay);
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }
    }

    @Nested
    @DisplayName("summarizeCategoriesByDay")
    class SummarizeCategoriesByDay {

        @Test
        @DisplayName("The stats of entries in the same category are added up.")
        void sameCategory() {
            // Arrange
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            setEntryStats(entry1, 1, 2, 3, 4, 5, 6);
            FoodEntry entry2 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 2);
            setEntryStats(entry2, 7, 8, 9, 10, 11, 12);
            flushAndClear();

            // Act
            List<CategoryStats> result = entryRepository.summarizeCategoriesByDay(myDay.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new CategoryStats(Category.LUNCH,
                                    BigDecimal.valueOf(8), BigDecimal.valueOf(10), BigDecimal.valueOf(12),
                                    BigDecimal.valueOf(14), BigDecimal.valueOf(16), BigDecimal.valueOf(18))
                    ));
        }

        @Test
        @DisplayName("Entries from different categories are not added up.")
        void differentCategories() {
            // Arrange
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            setEntryStats(entry1, 1, 2, 3, 4, 5, 6);
            FoodEntry entry2 = prepareEntryPositional(myUser, myDay, Category.DINNER, 2);
            setEntryStats(entry2, 7, 8, 9, 10, 11, 12);
            flushAndClear();

            // Act
            List<CategoryStats> result = entryRepository.summarizeCategoriesByDay(myDay.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new CategoryStats(Category.LUNCH,
                                    BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                                    BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(6)),
                            new CategoryStats(Category.DINNER,
                                    BigDecimal.valueOf(7), BigDecimal.valueOf(8), BigDecimal.valueOf(9),
                                    BigDecimal.valueOf(10), BigDecimal.valueOf(11), BigDecimal.valueOf(12))
                    ));

        }

        @Test
        @DisplayName("Entries from other days are not included in the calculations.")
        void otherDaysExcluded() {
            // Arrange
            Day invalidDay = prepareAdditionalDay(myPlan);
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            setEntryStats(entry1, 1, 2, 3, 4, 5, 6);
            FoodEntry entry2 = prepareEntryPositional(myUser, invalidDay, Category.LUNCH, 2);
            setEntryStats(entry2, 7, 8, 9, 10, 11, 12);
            flushAndClear();

            // Act
            List<CategoryStats> result = entryRepository.summarizeCategoriesByDay(myDay.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new CategoryStats(Category.LUNCH,
                                    BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                                    BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(6))
                    ));
        }
    }

    @Nested
    @DisplayName("summarizeDaysByPlan")
    class SummarizeDaysByPlan {

        @Test
        @DisplayName("The stats of entries from the same day are added up.")
        void sameDay() {
            // Arrange
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            setEntryStats(entry1, 1, 2, 3, 4, 5, 6);
            FoodEntry entry2 = prepareEntryPositional(myUser, myDay, Category.SNACK, 1);
            setEntryStats(entry2, 7, 8, 9, 10, 11, 12);
            flushAndClear();

            // Act
            List<DayStats> result = entryRepository.summarizeDaysByPlan(myPlan.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new DayStats(myDay.getId(),
                                    BigDecimal.valueOf(8), BigDecimal.valueOf(10), BigDecimal.valueOf(12),
                                    BigDecimal.valueOf(14), BigDecimal.valueOf(16), BigDecimal.valueOf(18))
                    ));
        }

        @Test
        @DisplayName("The stats of entries from different days are not added up.")
        void differentDays() {
            // Arrange
            Day mySecondDay = prepareAdditionalDay(myPlan);
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            setEntryStats(entry1, 1, 2, 3, 4, 5, 6);
            FoodEntry entry2 = prepareEntryPositional(myUser, mySecondDay, Category.SNACK, 1);
            setEntryStats(entry2, 7, 8, 9, 10, 11, 12);
            flushAndClear();

            // Act
            List<DayStats> result = entryRepository.summarizeDaysByPlan(myPlan.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new DayStats(myDay.getId(),
                                    BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                                    BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(6)),
                            new DayStats(mySecondDay.getId(),
                                    BigDecimal.valueOf(7), BigDecimal.valueOf(8), BigDecimal.valueOf(9),
                                    BigDecimal.valueOf(10), BigDecimal.valueOf(11), BigDecimal.valueOf(12))
                    ));
        }

        @Test
        @DisplayName("Entries from other plans are not included in the calculations.")
        void otherPlansExcluded() {
            // Arrange
            Plan mySecondPlan = prepareAdditionalPlan(myUser);
            Day mySecondDay = mySecondPlan.getDays().iterator().next();

            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            setEntryStats(entry1, 1, 2, 3, 4, 5, 6);
            FoodEntry entry2 = prepareEntryPositional(myUser, mySecondDay, Category.LUNCH, 1);
            setEntryStats(entry2, 7, 8, 9, 10, 11, 12);
            flushAndClear();

            // Act
            List<DayStats> result = entryRepository.summarizeDaysByPlan(myPlan.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new DayStats(myDay.getId(),
                                    BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                                    BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(6))
                    ));
        }

    }

    @Nested
    @DisplayName("extractShoppingListByPlan")
    class ExtractShoppingListByPlan {

        @Test
        @DisplayName("The grams of entries with the same name are added up.")
        void sameName() {
            // Arrange
            Day mySecondDay = prepareAdditionalDay(myPlan);
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            entry1.setName("Beans");
            entry1.setGrams(BigDecimal.valueOf(100));

            FoodEntry entry2 = prepareEntryPositional(myUser, mySecondDay, Category.SNACK, 1);
            entry2.setName("Beans");
            entry2.setGrams(BigDecimal.valueOf(250));

            flushAndClear();

            // Act
            List<ShopItem> result = entryRepository.extractShoppingListByPlan(myPlan.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new ShopItem("Beans", BigDecimal.valueOf(350))
                    ));
        }

        @Test
        @DisplayName("The grams of entries with different names are not added up.")
        void differentName() {
            // Arrange
            Day mySecondDay = prepareAdditionalDay(myPlan);
            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            entry1.setName("Beans");
            entry1.setGrams(BigDecimal.valueOf(100));

            FoodEntry entry2 = prepareEntryPositional(myUser, mySecondDay, Category.SNACK, 1);
            entry2.setName("Lentils");
            entry2.setGrams(BigDecimal.valueOf(250));

            flushAndClear();

            // Act
            List<ShopItem> result = entryRepository.extractShoppingListByPlan(myPlan.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new ShopItem("Beans", BigDecimal.valueOf(100)),
                            new ShopItem("Lentils", BigDecimal.valueOf(250))
                    ));
        }

        @Test
        @DisplayName("Entries from other plans are not included in the calculations.")
        void otherPlansExcluded() {
            // Arrange
            Plan mySecondPlan = prepareAdditionalPlan(myUser);
            Day mySecondDay = mySecondPlan.getDays().iterator().next();

            FoodEntry entry1 = prepareEntryPositional(myUser, myDay, Category.LUNCH, 1);
            entry1.setName("Beans");
            entry1.setGrams(BigDecimal.valueOf(100));

            FoodEntry entry2 = prepareEntryPositional(myUser, mySecondDay, Category.LUNCH, 1);
            entry2.setName("Beans");
            entry2.setGrams(BigDecimal.valueOf(250));

            flushAndClear();

            // Act
            List<ShopItem> result = entryRepository.extractShoppingListByPlan(myPlan.getId());

            // Assert
            assertThat(result)
                    .usingRecursiveComparison()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .isEqualTo(List.of(
                            new ShopItem("Beans", BigDecimal.valueOf(100))
                    ));
        }
    }

    @Nested
    @DisplayName("countByDayAndCategory")
    class CountByDayAndCategory {

        private static final int TARGET_COUNT = 3;
        private static final Category TARGET_CATEGORY = Category.LUNCH;
        private static final Category INVALID_CATEGORY = Category.DINNER;

        private List<Entry> prepareEntriesToCount() {
            int totalCount = TARGET_COUNT + 1;

            List<Entry> entries = new ArrayList<>(totalCount);
            for (int i = 1; i <= totalCount; i++) {
                FoodEntry entry = prepareEntryPositional(myUser, myDay, TARGET_CATEGORY, i);
                entries.add(entry);
            }

            return entries;
        }

        @Test
        @DisplayName("Does not count entries from other days.")
        void otherDaysExcluded() {
            // Arrange
            Day invalidDay = prepareAdditionalDay(myPlan);

            List<Entry> entries = prepareEntriesToCount();
            entries.getLast().setDay(invalidDay);

            flushAndClear();

            // Act
            int result = entryRepository.countByDayAndCategory(myDay.getId(), TARGET_CATEGORY);

            // Assert
            assertThat(result).as("Method output should match target count.")
                    .isEqualTo(TARGET_COUNT);
        }

        @Test
        @DisplayName("Does not count entries from other categories.")
        void otherCategoriesExcluded() {
            // Arrange
            List<Entry> entries = prepareEntriesToCount();
            entries.getLast().setCategory(INVALID_CATEGORY);
            flushAndClear();

            // Act
            int result = entryRepository.countByDayAndCategory(myDay.getId(), TARGET_CATEGORY);

            // Assert
            assertThat(result).as("Method output should match target count.")
                    .isEqualTo(TARGET_COUNT);
        }

    }

    @Nested
    @DisplayName("closeGapAfterRemoval")
    class CloseGapAfterRemoval {

        private static final Category TARGET_CATEGORY = Category.DINNER;

        private List<Entry> prepareEntriesToMove() {
            int[] positions = {1, 2, 4, 5};

            List<Entry> entries = new ArrayList<>(positions.length);
            for (int i : positions) {
                FoodEntry entry = prepareEntryPositional(myUser, myDay, TARGET_CATEGORY, i);
                entries.add(entry);
            }

            return entries;
        }

        @Test
        @DisplayName("Properly moves entries to close the gap in the specified day and category.")
        void gapClosed() {
            // Arrange
            List<Entry> entries = prepareEntriesToMove();
            flushAndClear();

            // Act
            entryRepository.closeGapAfterRemoval(myDay.getId(), TARGET_CATEGORY, 3);

            // Assert
            List<Long> ids = entries.stream().map(Entry::getId).toList();
            List<Entry> moved = ids.stream()
                    .map(id -> entityManager.find(Entry.class, id))
                    .toList();

            assertThat(moved)
                    .as("Entries after the empty spot should have been moved.")
                    .extracting(Entry::getId, Entry::getPosition)
                    .containsExactly(
                            tuple(ids.get(0), 1),   // original position = 1
                            tuple(ids.get(1), 2),   // original position = 2
                            tuple(ids.get(2), 3),   // original position = 4
                            tuple(ids.get(3), 4)    // original position = 5
                    );
        }

    }

    // deleteByDay
    // TODO: Write tests. Optional.

    // deleteByPlan
    // TODO: Write tests. Optional.
}
