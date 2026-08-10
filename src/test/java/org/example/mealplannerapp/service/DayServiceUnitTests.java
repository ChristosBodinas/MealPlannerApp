package org.example.mealplannerapp.service;

import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.mapper.EntryMapperImpl;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.DayTestFixtures.*;

import static org.assertj.core.api.Assertions.*;

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
        dayService = new DayService(entryRepository, dayRepository, entryMapper);
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

        @Test
        @DisplayName("Returns list of all entries that belong to the given day when it exists and belongs to the given user.")
        void entriesRetrieved() {
            // TODO: Write test.

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

    // TODO: summarizeDay tests

    
}
