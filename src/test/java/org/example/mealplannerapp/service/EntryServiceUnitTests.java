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
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.Tuple;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixtures.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixtures.FoodTestFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntryServiceUnitTests {

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

    private User user;

    @BeforeEach
    void prepareTests() {
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);
    }

    //<editor-fold desc="createEntry tests">
    @Test
    @DisplayName("createEntry successfully creates FoodEntry and saves to database.")
    void createEntry_withFoodEntry_happyFlow() {
        // Arrange
        FoodEntryCreateRequest request = defaultFoodEntryCreateRequest();
        FoodEntry entry = defaultFoodEntry();
        when(entryMapper.createFromRequest(request)).thenReturn(entry);

        Day day = new Day();
        when(dayRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(day));

        Food food = defaultFood();
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.of(food));

        when(entryRepository.countInDayAndCategory(88L, Category.BREAKFAST)).thenReturn(2L);

        FoodEntry saved = new FoodEntry();
        when(entryRepository.save(entry)).thenReturn(saved);

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(saved)).thenReturn(expected);

        // Act
        EntryResponse response = entryService.createEntry(user, 88L, request);

        // Assert
        assertThat(response).isEqualTo(expected);
        assertThat(entry.getDay()).isEqualTo(day);
        assertThat(entry.getFood()).isEqualTo(food);
        assertThat(entry.getPosition()).isEqualTo(3);
        assertThat(entry.getCalories()).isCloseTo(97.0, within(0.01));
        assertThat(entry.getProtein()).isCloseTo(12.0, within(0.01));
        assertThat(entry.getCarbs()).isCloseTo(37.5, within(0.01));
        assertThat(entry.getFat()).isCloseTo(4.5, within(0.01));
        assertThat(entry.getFiber()).isCloseTo(6.0, within(0.01));
        assertThat(entry.getPrice()).isCloseTo(3.77, within(0.01));
    }

    @Test
    @DisplayName("createEntry fails to find the requested day.")
    void createEntry_throwsDayNotFound() {
        // Arrange
        FoodEntryCreateRequest request = defaultFoodEntryCreateRequest();
        FoodEntry entry = new FoodEntry();
        when(entryMapper.createFromRequest(request)).thenReturn(entry);

        when(dayRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.createEntry(user, 88L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(entry.getDay()).isNull();
    }

    @Test
    @DisplayName("createEntry fails to find the requested food.")
    void createEntry_withFoodEntry_throwsFoodNotFound() {
        // Arrange
        FoodEntryCreateRequest request = defaultFoodEntryCreateRequest();
        FoodEntry entry = new FoodEntry();
        when(entryMapper.createFromRequest(request)).thenReturn(entry);

        Day day = new Day();
        when(dayRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(day));

        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.createEntry(user, 88L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(entry.getDay()).isEqualTo(day);
        assertThat(entry.getFood()).isNull();
    }
    //</editor-fold>

    //<editor-fold desc="duplicateEntry tests">
    @Test
    @DisplayName("duplicateEntry successfully copies the requested Entry to the requested Day.")
    void duplicateEntry_happyFlow() {
        // Arrange
        EntryDuplicateRequest request = new EntryDuplicateRequest(88L, Category.BREAKFAST);
        FoodEntry entry = defaultFoodEntry();
        Food food = defaultFood();
        entry.setFood(food);

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        Day day = new Day();
        when(dayRepository.findByIdVerified(1L, 77L)).thenReturn(Optional.of(day));

        when(entryRepository.countInDayAndCategory(77L, Category.BREAKFAST)).thenReturn(2L);

        FoodEntry saved = new FoodEntry();
        when(entryRepository.save(any())).thenReturn(saved);

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(saved)).thenReturn(expected);

        // Act
        EntryResponse response = entryService.duplicateEntry(user, 88L, request);

        // Assert

    }

    @Test
    @DisplayName("duplicateEntry fails to find the requested entry.")
    void duplicateEntry_throwsEntryNotFound() {
        // Arrange
        EntryDuplicateRequest request = new EntryDuplicateRequest(88L, Category.BREAKFAST);

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.duplicateEntry(user, 77L, request))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(dayRepository, never()).findByIdVerified(any(), any());
    }

    @Test
    @DisplayName("duplicateEntry fails to find the requested day.")
    void duplicateEntry_throwsDayNotFound() {
        // Arrange
        EntryDuplicateRequest request = new EntryDuplicateRequest(88L, Category.BREAKFAST);

        FoodEntry entry = new FoodEntry();
        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        when(dayRepository.findByIdVerified(1L, 77L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.duplicateEntry(user, 77L, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }
    //</editor-fold>

    //<editor-fold desc="editEntry tests">
    @Test
    @DisplayName("editEntry successfully edits the requested entry.")
    void editEntry_happyFlow() {
        // Arrange
        FoodEntry entry = new FoodEntry();
        FoodEntryEditRequest request = defaultFoodEntryEditRequest();

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(entry)).thenReturn(expected);

        // Act
        EntryResponse response = entryService.editEntry(user, 88L, request);

        // Assert
        assertThat(response).isEqualTo(expected);
        verify(entryMapper).updateFromRequest(entry, request);
    }

    @Test
    @DisplayName("editEntry fails to find the requested entry.")
    void editEntry_throwsEntryNotFound() {
        // Arrange
        FoodEntryEditRequest request = defaultFoodEntryEditRequest();

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.editEntry(user, 88L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(entryMapper, never()).updateFromRequest(any(), any());
    }
    //</editor-fold>

    //<editor-fold desc="moveEntry tests">

    @Test
    @DisplayName("moveEntry successfully moves the requested entry up in the same category.")
    void moveEntry_withSameCategoryUp_happyFlow() {
        // Arrange
        FoodEntry entry = foodEntryWithPosition(Category.LUNCH, 4);
        EntryMoveRequest request = new EntryMoveRequest(Category.LUNCH, 8);


    }

    @Test
    @DisplayName("moveEntry successfully moves the requested entry down in the same category.")
    void moveEntry_withSameCategoryDown_happyFlow() {

    }

    @ParameterizedTest
    @DisplayName("moveEntry successfully moves the requested entry in from another category.")
    @ValueSource(ints = {
        8,      // desiredPosition < categoryCount
        10,     // desiredPosition = categoryCount
        12      // desiredPosition > categoryCount
    })
    void moveEntry_withDifferentCategory_happyFlow(int desiredPosition) {
        // Arrange
        FoodEntry entry = foodEntryWithPosition(Category.LUNCH, 5);
        EntryMoveRequest request = new EntryMoveRequest(Category.LUNCH, desiredPosition);
        long categoryCount = 10;
        when(entryRepository.countInDayAndCategory(77L, Category.LUNCH)).thenReturn(categoryCount);

        // Act
        entryService.moveEntry(user, 77L, 88L, request);
    }

    @Test
    @DisplayName("moveEntry fails to find the requested entry.")
    void moveEntry_throwsEntryNotFound() {
        // Arrange
        EntryMoveRequest request = new EntryMoveRequest(Category.LUNCH, 4);
        when(entryRepository.findShallowByIdAndDayVerified(1L, 77L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.moveEntry(user, 77L, 88L, request))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(entryRepository, never()).countInDayAndCategory(any(), any());
    }
    //</editor-fold>

    //<editor-fold desc="delete Entry tests"
    @Test
    @DisplayName("deleteEntry successfully deletes the requested entry.")
    void deleteEntry_happyFlow() {
        // Arrange
        Tuple positionData = mock(Tuple.class);
        when(positionData.get("dayId", Long.class)).thenReturn(77L);
        when(positionData.get("category", Category.class)).thenReturn(Category.BREAKFAST);
        when(positionData.get("position", Integer.class)).thenReturn(5);
        when(entryRepository.findPositionDataByIdVerified(1L, 88L)).thenReturn(Optional.of(positionData));

        when(entryRepository.deleteByIdVerified(1L, 88L)).thenReturn(1);

        // Act
        entryService.deleteEntry(user, 88L);

        // Assert
        verify(entryRepository).deleteByIdVerified(1L, 88L);
        verify(entryRepository).shiftDownInDayAndCategory(77L, Category.BREAKFAST, 5, null);
    }

    @Test
    @DisplayName("deleteEntry fails to find the requested entry.")
    void deleteEntry_throwsEntryNotFound() {
        // Arrange
        when(entryRepository.findPositionDataByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.deleteEntry(user, 88L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
    //</editor-fold>

    //<editor-fold desc="retrieveEntry tests">
    @Test
    @DisplayName("retrieveEntry successfully returns the requested entry.")
    void retrieveEntry_happyFlow() {
        // Arrange
        FoodEntry entry = new FoodEntry();
        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(entry)).thenReturn(expected);

        // Act
        EntryResponse response = entryService.retrieveEntry(user, 88L);

        // Assert
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("retrieveEntry fails to find the requested entry.")
    void retrieveEntry_throwsEntryNotFound() {
        // Arrange
        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.retrieveEntry(user, 88L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    //</editor-fold>
}