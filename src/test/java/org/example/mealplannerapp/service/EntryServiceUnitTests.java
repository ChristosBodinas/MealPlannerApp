package org.example.mealplannerapp.service;


import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.PositionData;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixtures.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixtures.FoodTestFixtures.defaultFoodBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntryServiceUnitTests {

    // MOCKS AND INJECTION
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private EntryMapper entryMapper;
    @Mock
    private DayRepository dayRepository;
    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private EntryService entryService;

    // UNIVERSAL VARIABLES
    private User user;
    private static final Long USER_ID = 1L;
    private static final Long ENTRY_ID = 99L;
    private static final Long FOOD_ID = 88L;
    private static final Long DAY_ID = 77L;

    @Nested
    class createEntry {

        private FoodEntryCreateRequest request;
        private FoodEntry created;

        private static final long COUNT = 3;
        private static final int POSITION = 4;
        private static final Category CATEGORY_LUNCH = Category.LUNCH;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = defaultFoodEntryCreateRequestBuilder()
                    .foodId(FOOD_ID)
                    .category(CATEGORY_LUNCH)
                    .build();
            created = spy(defaultFoodEntryBuilder().category(CATEGORY_LUNCH).build());

            when(user.getId()).thenReturn(USER_ID);
            when(entryMapper.createFromRequest(request)).thenReturn(created);
        }

        @Test
        @DisplayName("Given a valid input, creates and saves the new Entry.")
        void happyFlow() {
            // Arrange
            Day day = new Day();
            Food food = defaultFoodBuilder().build();
            FoodEntry saved = new FoodEntry();
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(foodRepository.findByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));
            when(entryRepository.countInDayAndCategory(DAY_ID, CATEGORY_LUNCH)).thenReturn(COUNT);
            when(entryRepository.save(created)).thenReturn(saved);
            when(entryMapper.generateResponse(saved)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.createEntry(user, DAY_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            assertThat(created.getDay()).isEqualTo(day);
            assertThat(created.getFood()).isEqualTo(food);
            assertThat(created.getPosition()).isEqualTo(POSITION);
            verify(created).snapshotNutritionAndPriceInfo();
        }

        @Test
        @DisplayName("Given an invalid dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(dayRepository).findByIdVerified(USER_ID, DAY_ID);
            verify(foodRepository, never()).findByIdVerified(USER_ID, FOOD_ID);
            verify(created, never()).snapshotNutritionAndPriceInfo();
        }

        @Test
        @DisplayName("Given an invalid foodId, throws a ResourceNotFoundException.")
        void foodNotFound() {
            // Arrange
            Day day = new Day();

            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(foodRepository.findByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(dayRepository).findByIdVerified(USER_ID, DAY_ID);
            verify(foodRepository).findByIdVerified(USER_ID, FOOD_ID);
            verify(created, never()).snapshotNutritionAndPriceInfo();
        }

    }

    @Nested
    class duplicateEntry {

        private EntryDuplicateRequest request;
        private FoodEntry found;

        private static final Category COPY_CATEGORY = Category.DINNER;
        private static final long COUNT = 3L;
        private static final int COPY_POSITION = 4;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = new EntryDuplicateRequest(ENTRY_ID, COPY_CATEGORY);
            found = spy(defaultFoodEntryBuilder().build());
            Food food = defaultFoodBuilder().build();
            found.setFood(food);
            found.snapshotNutritionAndPriceInfo();

            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, creates and saves a duplicate of the requested Entry.")
        void happyFlow() {
            // Arrange
            Day day = new Day();
            ArgumentCaptor<FoodEntry> entryCaptor = ArgumentCaptor.forClass(FoodEntry.class);
            FoodEntry saved = new FoodEntry();
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.countInDayAndCategory(DAY_ID, COPY_CATEGORY)).thenReturn(COUNT);
            when(entryRepository.save(any())).thenReturn(saved);    // We don't have access to the new entry.
            when(entryMapper.generateResponse(saved)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.duplicateEntry(user, DAY_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(entryRepository).save(entryCaptor.capture());
            FoodEntry copy = entryCaptor.getValue();
            assertThat(copy).isNotSameAs(found);
            assertThat(copy.getDay()).isEqualTo(day);
            assertThat(copy.getCategory()).isEqualTo(COPY_CATEGORY);
            assertThat(copy.getPosition()).isEqualTo(COPY_POSITION);
            assertThat(copy.getCalories()).isCloseTo(found.getCalories(), within(0.01));
        }

        @Test
        @DisplayName("Given an invalid entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(dayRepository, never()).findByIdVerified(USER_ID, DAY_ID);
        }

        @Test
        @DisplayName("Given an invalid dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(dayRepository).findByIdVerified(USER_ID, DAY_ID);
            verify(found, never()).createDuplicate();
        }

    }

    @Nested
    class editEntry {

        private FoodEntryEditRequest request;
        private FoodEntry found;
        private static final double NEW_GRAMS = 200.0;
        private static final String NEW_UNIT = "cup";
        private static final String NEW_MERCHANT = "MyMarket";

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = new FoodEntryEditRequest(NEW_GRAMS, NEW_UNIT, NEW_MERCHANT);

            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and edits the requested Entry.")
        void happyFlow() {
            // Arrange
            FoodEntry found = spy(defaultFoodEntryBuilder().build());
            Food food = defaultFoodBuilder().build();
            found.setFood(food);
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryMapper.generateResponse(found)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.editEntry(user, ENTRY_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(entryMapper).updateFromRequest(found, request);
            verify(found).snapshotNutritionAndPriceInfo();
        }

        @Test
        @DisplayName("Given an invalid user or entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.editEntry(user, ENTRY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(entryMapper, never()).updateFromRequest(any(), any());
        }

    }

    @Nested
    class moveEntry {

        private EntryMoveRequest request;
        private FoodEntry found;

        private static final Category SAME_CATEGORY = Category.LUNCH;
        private static final Category DIFFERENT_CATEGORY = Category.DINNER;
        private static final int SOURCE_POSITION = 5;
        private static final long COUNT = 10;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            found = defaultFoodEntryBuilder()
                    .category(SAME_CATEGORY)
                    .position(SOURCE_POSITION)
                    .build();

            when(user.getId()).thenReturn(USER_ID);
        }

        @ParameterizedTest
        @DisplayName("Given a higher position in the same category, moves the requested Entry up in its current category.")
        @ValueSource(ints = {(int) COUNT - 2, (int) COUNT, (int) COUNT + 2})
        void sameCategoryUp(int desiredPosition) {
            // Arrange
            request = new EntryMoveRequest(SAME_CATEGORY, desiredPosition);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftDownInDayAndCategory(eq(DAY_ID), eq(SAME_CATEGORY), eq(SOURCE_POSITION), anyInt());
            verify(entryRepository, never()).shiftUpInDayAndCategory(any(), any(), any(), any());

            assertThat(found.getCategory()).isEqualTo(SAME_CATEGORY);
            assertThat(found.getPosition()).isGreaterThan(SOURCE_POSITION);
            assertThat(found.getPosition()).isLessThanOrEqualTo((int) COUNT);
        }

        @Test
        @DisplayName("Given a lower position in the same category, moves the requested Entry down in its current category.")
        void sameCategoryDown() {
            // Arrange
            request = new EntryMoveRequest(SAME_CATEGORY, SOURCE_POSITION - 2);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftUpInDayAndCategory(eq(DAY_ID), eq(SAME_CATEGORY), anyInt(), eq(SOURCE_POSITION));
            verify(entryRepository, never()).shiftDownInDayAndCategory(any(), any(), any(), any());

            assertThat(found.getCategory()).isEqualTo(SAME_CATEGORY);
            assertThat(found.getPosition()).isLessThan(SOURCE_POSITION);
        }

        @Test
        @DisplayName("Given the same position in the same category, performs no changes.")
        void unchangedPosition() {
            // Arrange
            request = new EntryMoveRequest(SAME_CATEGORY, SOURCE_POSITION);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository, never()).shiftDownInDayAndCategory(any(), any(), any(), any());
            verify(entryRepository, never()).shiftUpInDayAndCategory(any(), any(), any(), any());
            assertThat(found.getPosition()).isEqualTo(SOURCE_POSITION);
        }

        @ParameterizedTest
        @DisplayName("Given a different category, moves the requested Entry and closes the gap left behind.")
        @ValueSource(ints = {(int) COUNT - 2, (int) COUNT, (int) COUNT + 2})
        void differentCategory(int desiredPosition) {
            // Arrange
            request = new EntryMoveRequest(DIFFERENT_CATEGORY, desiredPosition);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, DIFFERENT_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftUpInDayAndCategory(eq(DAY_ID), eq(DIFFERENT_CATEGORY), eq(null), anyInt());
            verify(entryRepository).shiftDownInDayAndCategory(DAY_ID, SAME_CATEGORY, SOURCE_POSITION, null);

            assertThat(found.getCategory()).isEqualTo(DIFFERENT_CATEGORY);
            assertThat(found.getPosition()).isLessThanOrEqualTo((int) COUNT + 1);
        }

        @Test
        @DisplayName("Given an invalid entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Assert
            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.moveEntry(user, DAY_ID, ENTRY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, times(1)).findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID);
            verifyNoMoreInteractions(entryRepository);
        }

    }

    @Nested
    class deleteEntry {

        private static final Category ENTRY_CATEGORY = Category.BREAKFAST;
        private static final int ENTRY_POSITION = 3;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and deletes the requested Entry, then closes the gap left behind.")
        void happyFlow() {
            // Arrange
            PositionData data = mock(PositionData.class);

            when(entryRepository.findPositionDataByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(data));
            when(data.getDayId()).thenReturn(DAY_ID);
            when(data.getCategory()).thenReturn(ENTRY_CATEGORY);
            when(data.getPosition()).thenReturn(ENTRY_POSITION);

            // Act
            entryService.deleteEntry(user, ENTRY_ID);

            // Assert
            verify(entryRepository).findPositionDataByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository).deleteByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository).shiftDownInDayAndCategory(DAY_ID, ENTRY_CATEGORY, ENTRY_POSITION, null);
        }

        @Test
        @DisplayName("Given an invalid user or entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findPositionDataByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.deleteEntry(user, ENTRY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findPositionDataByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository, never()).deleteByIdVerified(any(), any());
            verify(entryRepository, never()).shiftDownInDayAndCategory(any(), any(), any(), any());
        }
    }

    @Nested
    class retrieveEntry {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and returns the requested Entry.")
        void happyFlow() {
            // Arrange
            FoodEntry entry = new FoodEntry();
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(entryMapper.generateResponse(entry)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.retrieveEntry(user, ENTRY_ID);

            // Assert
            assertThat(response).isEqualTo(expected);
        }

        @Test
        @DisplayName("Given an invalid user or entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.retrieveEntry(user, ENTRY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(entryMapper, never()).generateResponse(any());
        }

    }

}