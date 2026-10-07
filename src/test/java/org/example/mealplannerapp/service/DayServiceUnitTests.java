package org.example.mealplannerapp.service;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.StatsResponse;
import org.example.mealplannerapp.dto.day.response.CategoryStatsResponse;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.listed.ListedEntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.*;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDay;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultExerciseEntry;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntry;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExercise;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DayServiceUnitTests {

    // CONSTANTS
    private static final long USER_ID = 1L;
    private static final long DAY_ID = 99L;

    // BEANS
    private DayService dayService;
    private DayMapper dayMapper;
    private EntryMapper entryMapper;

    @Mock
    private DayRepository dayRepository;

    @Mock
    private EntryRepository entryRepository;

    // VARIABLES
    private User myUser;

    // HELPER METHODS

    // TESTS PROPER
    @BeforeEach
    void prepareServiceAndUser() {
        dayMapper = new DayMapperImpl();
        entryMapper = new EntryMapperImpl(new FoodMapperImpl(), new ExerciseMapperImpl());
        dayService = new DayService(dayRepository, entryRepository, dayMapper, entryMapper);

        myUser = defaultUser().id(USER_ID).build();
    }

    @Nested
    @DisplayName("deleteAllEntries")
    class DeleteAllEntries {

        @Test
        @DisplayName("Given an existing dayId owned by the current user, deletes all entries in that day.")
        void allEntriesDeleted() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(true);

            // Act + Assert
            assertThatCode(() -> dayService.deleteAllEntries(myUser, DAY_ID))
                    .as("Method should throw no exceptions.")
                    .doesNotThrowAnyException();

            verify(entryRepository, description("Entries deleted.")).deleteByDay(DAY_ID);
        }

        @Test
        @DisplayName("Given a non-existent or non-owned dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> dayService.deleteAllEntries(myUser, DAY_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(entryRepository);
        }
    }

    @Nested
    @DisplayName("retrieveAllEntries")
    class RetrieveAllEntries {

        private FoodEntry prepareFoodEntry() {
            Food food = defaultFood().user(myUser).build();
            FoodEntry entry = defaultFoodEntry().food(food).build();
            entry.snapshotInfo();
            return entry;
        }

        private ExerciseEntry prepareExerciseEntry() {
            Exercise exercise = defaultExercise().user(myUser).build();
            ExerciseEntry entry = defaultExerciseEntry().exercise(exercise).build();
            entry.snapshotInfo();
            return entry;
        }

        @Test
        @DisplayName("Given an existing dayId owned by the current user, returns a list of ListedEntryResponses.")
        void allEntriesRetrieved() {
            // Arrange
            FoodEntry entry1 = prepareFoodEntry();
            ExerciseEntry entry2 = prepareExerciseEntry();
            List<Entry> entries = List.of(entry1, entry2);

            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(true);
            when(entryRepository.fetchShallowByDayOrdered(DAY_ID)).thenReturn(entries);

            // Act
            List<ListedEntryResponse> response = dayService.retrieveAllEntries(myUser, DAY_ID);

            // Assert
            assertThat(response).as("Method outputs should match mapper outputs.")
                    .containsExactly(
                            entryMapper.toListedResponse(entry1),
                            entryMapper.toListedResponse(entry2));
        }

        @Test
        @DisplayName("Given a non-existent or non-owned dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> dayService.retrieveAllEntries(myUser, DAY_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(entryRepository);
        }

    }

    @Nested
    @DisplayName("summarizeDay")
    class SummarizeDay {

        private final int numCat = Category.values().length;

        private CategoryStats prepareCategoryStats(Category category, int calories, int protein,
                                              int carbs, int fat, int fiber, int price) {
            return new CategoryStats(
                    category,
                    BigDecimal.valueOf(calories),
                    BigDecimal.valueOf(protein),
                    BigDecimal.valueOf(carbs),
                    BigDecimal.valueOf(fat),
                    BigDecimal.valueOf(fiber),
                    BigDecimal.valueOf(price));
        }

        private CategoryStatsResponse prepareCategoryStatsResponse(Category category, int calories, int protein,
                                                              int carbs, int fat, int fiber, int price) {
            return new CategoryStatsResponse(
                    category,
                    BigDecimal.valueOf(calories),
                    BigDecimal.valueOf(protein),
                    BigDecimal.valueOf(carbs),
                    BigDecimal.valueOf(fat),
                    BigDecimal.valueOf(fiber),
                    BigDecimal.valueOf(price));
        }

        @Test
        @DisplayName("Given an existing dayId owned by the current user, returns a DaySummaryResponse.")
        void daySummarized() {
            // Arrange
            Day day = defaultDay().id(DAY_ID).build();

            List<CategoryStats> categoryStats = new ArrayList<>(numCat);
            for (Category category : Category.values()) {
                categoryStats.add(prepareCategoryStats(category, 1, 2, 3, 4, 5, 6));
            }

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.summarizeCategoriesByDay(DAY_ID)).thenReturn(categoryStats);

            // Act
            DaySummaryResponse response = dayService.summarizeDay(myUser, DAY_ID);

            // Assert
            assertThat(response.day()).as("Day should be properly returned.")
                    .isEqualTo(dayMapper.toResponse(day));

            assertThat(response.dayStats()).as("Day stats should be properly calculated.")
                    .extracting(StatsResponse::calories, StatsResponse::protein, StatsResponse::carbs,
                            StatsResponse::fat, StatsResponse::fiber, StatsResponse::price)
                    .containsExactly(
                            BigDecimal.valueOf(numCat), BigDecimal.valueOf(2 * numCat), BigDecimal.valueOf(3 * numCat),
                            BigDecimal.valueOf(4 * numCat), BigDecimal.valueOf(5 * numCat), BigDecimal.valueOf(6 * numCat));

            List<CategoryStatsResponse> categoryStatsResponses = new ArrayList<>(numCat);
            for (Category category : Category.values()) {
                categoryStatsResponses.add(prepareCategoryStatsResponse(category, 1, 2, 3, 4, 5, 6));
            }

            assertThat(response.categoryStats()).as("Category stats should be properly returned.")
                    .containsExactlyElementsOf(categoryStatsResponses);
        }

        @Test
        @DisplayName("Includes categories without any entries in the returned response.")
        void missingCategoriesFilled() {
            // Arrange
            Day day = defaultDay().id(DAY_ID).build();

            List<CategoryStats> categoryStats = new ArrayList<>(numCat);
            for (Category category : Category.values()) {
                if (category != Category.LUNCH) {
                    categoryStats.add(prepareCategoryStats(category, 1, 2, 3, 4, 5, 6));
                }
            }

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.summarizeCategoriesByDay(DAY_ID)).thenReturn(categoryStats);

            // Act
            DaySummaryResponse response = dayService.summarizeDay(myUser, DAY_ID);

            // Assert
            List<CategoryStatsResponse> categoryStatsResponses = new ArrayList<>(numCat);
            for (Category category : Category.values()) {
                if (category != Category.LUNCH) {
                    categoryStatsResponses.add(prepareCategoryStatsResponse(category, 1, 2, 3, 4, 5, 6));
                } else {
                    categoryStatsResponses.add(prepareCategoryStatsResponse(category, 0, 0, 0, 0, 0, 0));
                }
            }

            assertThat(response.categoryStats()).as("Category stats should be properly returned.")
                    .containsExactlyElementsOf(categoryStatsResponses);
        }

        @Test
        @DisplayName("Given a non-existent or non-owned dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> dayService.summarizeDay(myUser, DAY_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(entryRepository);
        }
    }
}
