package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.PriceResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.UnitResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FoodServiceUnitTests {

    @Mock private FoodRepository foodRepository;
    @Mock private FoodMapper foodMapper;

    @InjectMocks private FoodService foodService;

    private FoodRequest defaultFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitRequest("tbsp", 15.0),
                        new UnitRequest("cup", 235.0)),
                Set.of(new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupUnitFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitRequest("tbsp", 15.0),
                        new UnitRequest("tbsp", 235.0)),
                Set.of(new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupPriceFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitRequest("tbsp", 15.0),
                        new UnitRequest("cup", 235.0)),
                Set.of(new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("Masoutis", 5.70, 175)));
    }

    private FoodResponse defaultFoodResponse() {
        return new FoodResponse(
                99L, "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitResponse("tbsp", 15.0),
                        new UnitResponse("cup", 235.0)),
                Set.of(new PriceResponse("Masoutis", 6.80, 200),
                        new PriceResponse("MyMarket", 5.70, 175)));
    }

    private FoodResponse alternateFoodResponse() {
        return new FoodResponse(
                99L, "Mock Meal", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitResponse("tbsp", 16.0),
                        new UnitResponse("cup", 235.0)),
                Set.of(new PriceResponse("Masoutis", 6.80, 200),
                        new PriceResponse("MyMarket", 5.80, 175)));
    }

    @BeforeEach
    void prepareTests() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);       
    }

    // CREATE FOOD
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
        FoodRequest request;
        if (dupeType == "units") {
                request = dupUnitFoodRequest();
        } else if (dupeType == "prices") {
                request = dupPriceFoodRequest();
        }

        // Act + Assert
        assertThatThrownBy(() -> foodService.createFood(user, request))
                .isInstanceOf(IllegalDuplicateValueException.class);
        verify(foodMapper, never()).createFromRequest(request);
    }

    // UPDATE FOOD
    @Test
    @DisplayName("updateFood successfully updates food.")
    void updateFood_happyFlow() {
        // Arrange
        FoodRequest request = defaultFoodRequest();
        Food found = new Food();
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(found);

        FoodResponse expected = defaultFoodResponse();
        when(foodMapper.generateResponse(found)).thenReturn(expected);

        // Act
        FoodResponse response = foodService.updateFood(user, 99L, request);

        // Assert
        assertThat(response).isEqualTo(expected);
        verify(foodMapper).updateFromRequest(food, request);
    }

    @ParameterizedTest
    @DisplayName("updateFood rejects duplicate unit/price values.")
    @ValueSource(strings = {"units", "prices"})
    void updateFood_throwsIllegalDuplicateValue() {
        // Arrange
        FoodRequest request;
        if (dupeType == "units") {
                request = dupUnitFoodRequest();
        } else if (dupeType == "prices") {
                request = dupPriceFoodRequest();
        }

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFood(user, 99L, request))
                .isInstanceOf(IllegalDuplicateValueException.class);
        verify(foodRepository, never()).findByIdVerified(1L, 99L);
    }

    @Test // parameterized
    @DisplayName("updateFood fails to find the requested food.")
    void updateFood_throwsResourceNotFound() {
        // Arrange
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFood(user, 99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodMapper, never()).updateFromRequest();
    }

    // DELETE FOOD
    @Test
    @DisplayName("deleteFood successfully deletes food from database.")
    void deleteFood_happyFlow() {
        // Arrange
        when(foodRepository.deleteByIdVerified(1L, 99L)).thenReturn(1);

        // Act
        foodService.deleteFood(user, 99L);

        // Assert
        verify(foodRepository).deleteByIdVerified(1L, 99L);
    }

    @Test
    @DisplayName("deleteFood fails to find the requested food.")
    void deleteFood_throwsResourceNotFound() {
        // Arrange
        when(foodRepository.deleteByIdVerified(1L, 99L)).thenReturn(0);

        // Act + Assert
        assertThatThrownBy(() -> foodService.deleteFood(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(foodRepository).deleteByIdVerified(1L, 99L);
    }

    // RETRIEVE FOOD
    @Test
    @DisplayName("retrieveFood successfully returns the requested food.")
    void retrieveFood_happyFlow() {
        // Arrange       
        Food found = new Food();
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(found);

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
        User user = mock(User.class);
        when(user.getId())

    }

    // searchFoods tests: happy flow

    @Test
    @DisplayName("searchFood successfully returns foods.")
    void searchFoods_happyFlow() {
        // Arrange

        // Act

        // Assert

    }

}