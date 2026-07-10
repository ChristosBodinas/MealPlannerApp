package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.IllegalDuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.FoodMapper;
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
import static org.example.mealplannerapp.service.FoodTestFixtures.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceUnitTests {

    @Mock private FoodRepository foodRepository;
    @Mock private FoodMapper foodMapper;

    @InjectMocks private FoodService foodService;

    private User user;

    //</editor-fold>

    @BeforeEach
    void prepareTests() {
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);
    }

    //<editor-fold desc="createFood tests">
    @Test
    @DisplayName("createFood successfully saves food to database.")
    void createFood_happyFlow() {
        // Arrange
        FoodRequest request = defaultFoodRequest();
        Food created = new Food();
        when(foodMapper.createFromRequest(request)).thenReturn(created);

        Food saved = new Food();
        when(foodRepository.save(created)).thenReturn(saved);

        FoodResponse expected = defaultFoodResponse();
        when(foodMapper.generateResponse(saved)).thenReturn(expected);

        // Act
        FoodResponse response = foodService.createFood(user, request);

        // Assert
        assertThat(response).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("createFood correctly rejects duplicate unit/price values.")
    @ValueSource(strings = {"units", "prices"})
    void createFood_throwsIllegalDuplicateValue(String dupeType) {
        // Arrange
        FoodRequest request = dupeType.equals("units") ? duplicateUnitsRequest() : duplicatePricesRequest();

        // Act + Assert
        assertThatThrownBy(() -> foodService.createFood(user, request))
                .isInstanceOf(IllegalDuplicateValueException.class);
        verify(foodMapper, never()).createFromRequest(request);
    }
    //</editor-fold>

    //<editor-fold desc="updateFood tests">
    @Test
    @DisplayName("updateFood successfully updates food.")
    void updateFood_happyFlow() {
        // Arrange
        FoodRequest request = defaultFoodRequest();
        Food found = new Food();
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.of(found));

        FoodResponse expected = defaultFoodResponse();
        when(foodMapper.generateResponse(found)).thenReturn(expected);

        // Act
        FoodResponse response = foodService.updateFood(user, 99L, request);

        // Assert
        assertThat(response).isEqualTo(expected);
        verify(foodMapper).updateFromRequest(found, request);
    }

    @ParameterizedTest
    @DisplayName("updateFood rejects duplicate unit/price values.")
    @ValueSource(strings = {"units", "prices"})
    void updateFood_throwsIllegalDuplicateValue(String dupeType) {
        // Arrange
        FoodRequest request = dupeType.equals("units") ? duplicateUnitsRequest() : duplicatePricesRequest();

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFood(user, 99L, request))
                .isInstanceOf(IllegalDuplicateValueException.class);
        verify(foodRepository, never()).findByIdVerified(any(), any());
    }

    @Test
    @DisplayName("updateFood fails to find the requested food.")
    void updateFood_throwsResourceNotFound() {
        // Arrange
        FoodRequest request = defaultFoodRequest();
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFood(user, 99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodMapper, never()).updateFromRequest(any(), any());
    }
    //</editor-fold>

    //<editor-fold desc="deleteFood tests">
    @Test
    @DisplayName("deleteFood successfully deletes food from database.")
    void deleteFood_happyFlow() {
        // Arrange
        when(foodRepository.deleteByIdVerified(1L, 99L)).thenReturn(1);

        // Act
        foodService.deleteFood(user, 99L);

        // Assert
        verify(foodRepository).deleteByIdVerified(any(), any());
    }

    @Test
    @DisplayName("deleteFood fails to find the requested food.")
    void deleteFood_throwsResourceNotFound() {
        // Arrange
        when(foodRepository.deleteByIdVerified(1L, 99L)).thenReturn(0);

        // Act + Assert
        assertThatThrownBy(() -> foodService.deleteFood(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodRepository).deleteByIdVerified(any(), any());
    }
    //</editor-fold>

    //<editor-fold desc="retrieveFood tests">
    @Test
    @DisplayName("retrieveFood successfully returns the requested food.")
    void retrieveFood_happyFlow() {
        // Arrange       
        Food found = new Food();
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.of(found));

        FoodResponse expected = defaultFoodResponse();
        when(foodMapper.generateResponse(found)).thenReturn(expected);

        // Act
        FoodResponse response = foodService.retrieveFood(user, 99L);

        // Assert
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("retrieveFood fails to find the requested food.")
    void retrieveFood_throwsResourceNotFound() {
        // Arrange
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> foodService.retrieveFood(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodMapper, never()).generateResponse(any());
    }
    //</editor-fold>

    //<editor-fold desc="searchFoods tests">
    @Test
    @DisplayName("searchFood successfully returns foods.")
    void searchFoods_happyFlow() {
        // Arrange
        String search = "text";
        Food food1 = new Food();
        Food food2 = new Food();
        when(foodRepository.searchByText(1L, "text")).thenReturn(List.of(food1, food2));

        List<ListedFoodResponse> expected = listedResponseList(2);
        when(foodMapper.generateListedResponse(food1)).thenReturn(expected.get(0));
        when(foodMapper.generateListedResponse(food2)).thenReturn(expected.get(1));

        // Act
        List<ListedFoodResponse> responses = foodService.searchFoods(user, search);

        // Assert
        assertThat(responses).containsExactlyElementsOf(expected);
    }
    //</editor-fold>

}