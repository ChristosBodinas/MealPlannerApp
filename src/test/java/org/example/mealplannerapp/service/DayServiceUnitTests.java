package org.example.mealplannerapp.service;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.DayMapper;
import org.example.mealplannerapp.mapper.DayMapperImpl;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.mapper.EntryMapperImpl;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.impl.CategoryStatsImpl;
import org.example.mealplannerapp.projection.impl.StatsImpl;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDayBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultExerciseEntryBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntryBuilder;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DayServiceUnitTests {

    // MOCKS, SPIES, CAPTORS
    @Mock
    private DayRepository dayRepository;
    @Mock
    private EntryRepository entryRepository;

    // VARIABLES
    private DayService dayService;
    private EntryMapper entryMapper;
    private DayMapper dayMapper;
    private User myUser;
    private Plan myPlan;

    // CONSTANTS
    private static final long USER_ID = 1L;
    private static final long PLAN_ID = 66L;
    private static final long DAY_ID = 55L;

    // HELPER METHODS
    private Day prepareMyDay() {
        Day day = defaultDayBuilder().id(DAY_ID).plan(myPlan).build();
        myPlan.getDays().add(day);
        return day;
    }

    // BEFORE EACH
    @BeforeEach
    void prepareAllTests() {
        myUser = defaultUserBuilder().id(USER_ID).build();
        myPlan = defaultPlanBuilder().id(PLAN_ID).user(myUser).build();

        entryMapper = new EntryMapperImpl();
        dayMapper = new DayMapperImpl();
        dayService = new DayService(entryRepository, dayRepository, entryMapper, dayMapper);
    }

    // TESTS PROPER
    @Nested
    @DisplayName("deleteAllEntries")
    class DeleteAllEntries {

        @Test
        @DisplayName("Deletes all entries that belong to the given day if the day exists and belongs to another user.")
        void entriesDeleted() {
            // Arrange
            Day day = prepareMyDay();

            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(true);
            when(entryRepository.deleteByDay(DAY_ID)).thenReturn(5);

            // Act
            assertThatCode(() -> dayService.deleteAllEntries(myUser, DAY_ID))
                    .doesNotThrowAnyException();

            // Assert
            verify(entryRepository).deleteByDay(DAY_ID);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the given day does not exist or belongs to another user.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> dayService.deleteAllEntries(myUser, DAY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("retrieveAllEntries")
    class RetrieveAllEntries {

        List<Entry> prepareEntryList() {
            List<Entry> entries = new ArrayList<>(2);
            entries.add(defaultFoodEntryBuilder().build());
            entries.add(defaultExerciseEntryBuilder().build());
            return entries;
        }

        @Test
        @DisplayName("Returns list of all entries that belong to the given day when it exists and belongs to the given user.")
        void entriesRetrieved() {
            // Arrange
            List<Entry> entries = prepareEntryList();

            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(true);
            when(entryRepository.fetchByDayOrdered(DAY_ID)).thenReturn(entries);

            // Act
            List<EntryResponse> result = dayService.retrieveAllEntries(myUser, DAY_ID);

            // Assert
            assertThat(result).isEqualTo(entries.stream().map(entryMapper::toResponse).toList());
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the given day does not exist or belongs to another user.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> dayService.retrieveAllEntries(myUser, DAY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("summarizeDay")
    class SummarizeDay {

        List<CategoryStats> prepareCategoryStats() {
            List<CategoryStats> categoryStats = new ArrayList<>(3);
            categoryStats.add(new CategoryStatsImpl(Category.BREAKFAST, 100.0, 25.0, 40.0, 15.0, 5.0, 3.0));
            categoryStats.add(new CategoryStatsImpl(Category.LUNCH, 120.0, 30.0, 50.0, 10.0, 3.0, 4.0));
            categoryStats.add(new CategoryStatsImpl(Category.DINNER, 80.0, 15.0, 30.0, 5.0, 2.0, 3.0));
            return categoryStats;
        }

        @Test
        @DisplayName("Returns a summary of day goals, day stats, and category stats when the requested day exists and belongs to the given user.")
        void daySummarized() {
            // Arrange
            Day found = prepareMyDay();
            List<CategoryStats> categoryStats = prepareCategoryStats();

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.sumSnapshotsByDayGroupedByCategory(DAY_ID)).thenReturn(categoryStats);

            // Act
            DaySummaryResponse result = dayService.summarizeDay(myUser, DAY_ID);

            // TODO: Modify to check for zero-value stats.

            // Assert
            StatsImpl target = new StatsImpl(300.0, 70.0, 120.0, 30.0, 10.0, 10.0);
            assertThat(result).isEqualTo(dayMapper.toSummaryResponse(categoryStats, target, found.retrieveGoals()));

            assertThat(categoryStats).hasSizeGreaterThan(3);  // More than 3 elements.
            assertThat(categoryStats).extracting(
                    CategoryStats::getCategory,
                    CategoryStats::getCalories,
                    CategoryStats::getProtein,
                    CategoryStats::getCarbs,
                    CategoryStats::getFat,
                    CategoryStats::getFiber,
                    CategoryStats::getPrice
            ).contains(
                    tuple(Category.UNSORTED, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                    // Not checking other empty categories because the actual number and names might change.
            );
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the given day does not exist or belongs to another user.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> dayService.summarizeDay(myUser, DAY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

        }

    }


}
