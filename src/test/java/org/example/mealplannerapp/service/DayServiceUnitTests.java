package org.example.mealplannerapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.mealplannerapp.fixtures.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixtures.DayTestFixtures.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.mealplannerapp.dto.day.DayGoalsResponse;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DayServiceUnitTests {

    // MOCKS AND INJECTION
    @Mock private DayRepository dayRepository;
    @Mock private EntryRepository entryRepository;
    @Mock private EntryMapper entryMapper;

    @InjectMocks private DayService dayService;

    // UNIVERSAL VARIABLES
    private User user;
    private static final Long USER_ID = 1L;
    private static final Long DAY_ID = 77L;

    @Nested
    class deleteAllEntries {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid dayId, deletes all entries in the requested day.")
        void happyFlow() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(true);
            
            // Act
            dayService.deleteAllEntries(user, DAY_ID);

            // Assert
            verify(dayRepository).existsByIdVerified(USER_ID, DAY_ID);
            verify(entryRepository).deleteAllInDay(DAY_ID);
        }

        @Test
        @DisplayName("Given an invalid dayId or user, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> dayService.deleteAllEntries(user, DAY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(entryRepository);
        }

    }

    @Nested
    class retrieveAllEntries {

        List<FoodEntry> listedFoodEntries() {
            return List.of(
                defaultFoodEntryBuilder().id(7L).build(),
                defaultFoodEntryBuilder().id(13L).build()
            );
        }

        List<FoodEntryResponse> listedFoodEntryResponses() {
            return List.of(
                defaultFoodEntryResponseBuilder().id(7L).build(),
                defaultFoodEntryResponseBuilder().id(13L).build()
            );
        }

        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid dayId, returns all the entries in the requested day ordered " +
            "by category and position.")
        void happyFlow() {
            // Arrange
            List<Entry> entries = new ArrayList<>(listedFoodEntries());
            List<FoodEntryResponse> expected = listedFoodEntryResponses();

            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(true);
            when(entryRepository.findAllInDayOrdered(DAY_ID)).thenReturn(entries);
            when(entryMapper.generateResponse(entries.get(0))).thenReturn(expected.get(0));
            when(entryMapper.generateResponse(entries.get(1))).thenReturn(expected.get(1));

            // Act
            List<EntryResponse> response = dayService.retrieveAllEntries(user, DAY_ID);

            // Assert
            assertThat(response).isEqualTo(expected);
        }

        
        @Test
        @DisplayName("Given an invalid dayId or user, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.existsByIdVerified(USER_ID, DAY_ID)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> dayService.retrieveAllEntries(user, DAY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(entryRepository);
        }

    }
   
    @Nested
    class retrieveDaySummary {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid dayId, calculates and returns the per-category and overall nutrition totals.")
        void happyFlow() {

        }

        @Test
        @DisplayName("Given an invalid dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {

        }
    }
    @Nested
    class retrieveDayGoals {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid dayId, returns the nutrition goals for the requested day.")
        void happyFlow() {
            // Arrange
            Day found = defaultDayBuilder().build();
            DayGoalsResponse expected = defaultDayGoalsResponseBuilder().build();

            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(found));

            // Act
            DayGoalsResponse response = dayService.retrieveDayGoals(user, DAY_ID);

            // Assert
            assertThat(response).isEqualTo(expected);
        }

        @Test
        @DisplayName("Given an invalid dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> dayService.retrieveDayGoals(user, DAY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

}
