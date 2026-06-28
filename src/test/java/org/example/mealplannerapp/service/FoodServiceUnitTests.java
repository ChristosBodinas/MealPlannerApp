package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.FoodPriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.FoodUnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodPriceResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.FoodUnitResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.IllegalDuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ResourceNotOwnedException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceUnitTests {

    @Mock
    private FoodRepository foodRepository;
    @Mock private FoodMapper foodMapper;

    @InjectMocks
    private FoodService foodService;

    private FoodRequest defaultFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitRequest("tbsp", 15.0),
                        new FoodUnitRequest("cup", 235.0)),
                Set.of(new FoodPriceRequest("Masoutis", 6.80, 200),
                        new FoodPriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupUnitFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitRequest("tbsp", 15.0),
                        new FoodUnitRequest("tbsp", 235.0)),
                Set.of(new FoodPriceRequest("Masoutis", 6.80, 200),
                        new FoodPriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupPriceFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitRequest("tbsp", 15.0),
                        new FoodUnitRequest("cup", 235.0)),
                Set.of(new FoodPriceRequest("Masoutis", 6.80, 200),
                        new FoodPriceRequest("Masoutis", 5.70, 175)));
    }

    private FoodResponse defaultFoodResponse() {
        return new FoodResponse(
                99L, "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitResponse("tbsp", 15.0),
                        new FoodUnitResponse("cup", 235.0)),
                Set.of(new FoodPriceResponse("Masoutis", 6.80, 200),
                        new FoodPriceResponse("MyMarket", 5.70, 175)));
    }

    private FoodResponse alternateFoodResponse() {
        return new FoodResponse(
                99L, "Mock Meal", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitResponse("tbsp", 16.0),
                        new FoodUnitResponse("cup", 235.0)),
                Set.of(new FoodPriceResponse("Masoutis", 6.80, 200),
                        new FoodPriceResponse("MyMarket", 5.80, 175)));
    }

    @Test
    void createNewFood_happyFlow() {
        // Arrange
        User user = new User();
        FoodRequest request = defaultFoodRequest();

        Food mappedFood = new Food();
        when(foodMapper.createFromRequest(request)).thenReturn(mappedFood);

        Food savedFood = new Food();
        when(foodRepository.save(mappedFood)).thenReturn(savedFood);

        FoodResponse expectedResponse = defaultFoodResponse();
        when(foodMapper.generateResponse(savedFood)).thenReturn(expectedResponse);

        // Act
        FoodResponse actualResponse = foodService.createNewFood(user, request);

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(mappedFood.getUser()).isEqualTo(user);
    }

    @Test
    void createNewFood_dupedUnits() {
        // Arrange
        User user = new User();
        FoodRequest request = dupUnitFoodRequest();

        // Act + Assert
        assertThatThrownBy(() -> foodService.createNewFood(user, request))
            .isInstanceOf(IllegalDuplicateValueException.class);
    }

    @Test
    void createNewFood_dupedPrices() {
        // Arrange
        User user = new User();
        FoodRequest request = dupPriceFoodRequest();

        // Act + Assert
        assertThatThrownBy(() -> foodService.createNewFood(user, request))
            .isInstanceOf(IllegalDuplicateValueException.class);
    }

    @Test
    void updateFoodById_happyFlow() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Food existingFood = new Food();
        existingFood.setUser(user);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        FoodRequest request = defaultFoodRequest();

        FoodResponse expectedResponse = defaultFoodResponse();
        when(foodMapper.generateResponse(existingFood)).thenReturn(expectedResponse);

        // Act
        FoodResponse actualResponse = foodService.updateFoodById(user, 99L, request);

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(foodMapper).updateFromRequest(existingFood, request);
    }

    @Test
    void updateFoodById_dupedUnits() {
        // Arrange
        User user = new User();
        FoodRequest request = dupUnitFoodRequest();

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFoodById(user, 99L, request))
            .isInstanceOf(IllegalDuplicateValueException.class);
    }

    @Test
    void updateFoodById_dupedPrices() {
        // Arrange
        User user = new User();
        FoodRequest request = dupPriceFoodRequest();

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFoodById(user, 99L, request))
            .isInstanceOf(IllegalDuplicateValueException.class);
    }

    @Test
    void updateFoodById_foodNotFound() {
        // Arrange
        User user = new User();
        FoodRequest request = defaultFoodRequest();

        when(foodRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFoodById(user, 99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateFoodById_foodNotOwned() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(2L);

        FoodRequest request = defaultFoodRequest();

        Food existingFood = new Food();
        existingFood.setUser(owner);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateFoodById(user, 99L, request))
                .isInstanceOf(ResourceNotOwnedException.class);
    }

    @Test
    void retrieveFoodById_happyFlow() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Food existingFood = new Food();
        existingFood.setUser(user);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        FoodResponse expectedResponse = defaultFoodResponse();
        when(foodMapper.generateResponse(existingFood)).thenReturn(expectedResponse);

        // Act
        FoodResponse actualResponse = foodService.retrieveFoodById(user, 99L);

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void retrieveFoodById_foodNotFound() {
        // Arrange
        User user = new User();
        when(foodRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> foodService.retrieveFoodById(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void retrieveFoodById_foodNotOwned() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(2L);

        Food existingFood = new Food();
        existingFood.setUser(owner);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        // Act + Assert
        assertThatThrownBy(() -> foodService.retrieveFoodById(user, 99L))
                .isInstanceOf(ResourceNotOwnedException.class);
    }

    @Test
    void retrieveFoodByTextSearch_happyFlow() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Food food1 = new Food();
        Food food2 = new Food();

        FoodResponse response1 = defaultFoodResponse();
        FoodResponse response2 = alternateFoodResponse();

        when(foodRepository.findAllByUserIdAndTextSearch(1L, "text"))
                .thenReturn(Set.of(food1, food2));
        when(foodMapper.generateResponse(food1)).thenReturn(response1);
        when(foodMapper.generateResponse(food2)).thenReturn(response2);

        Set<FoodResponse> expectedResponse = Set.of(response1, response2);

        // Act
        Set<FoodResponse> actualResponse = foodService.retrieveFoodsByTextSearch(user, "text");

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(foodRepository).findAllByUserIdAndTextSearch(1L, "text");
    }

    @Test
    void retrieveAllFoods_happyFlow() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Food food1 = new Food();
        Food food2 = new Food();

        FoodResponse response1 = defaultFoodResponse();
        FoodResponse response2 = alternateFoodResponse();

        when(foodRepository.findAllByUserId(1L)).thenReturn(Set.of(food1, food2));
        when(foodMapper.generateResponse(food1)).thenReturn(response1);
        when(foodMapper.generateResponse(food2)).thenReturn(response2);

        Set<FoodResponse> expectedResponse = Set.of(response1, response2);

        // Act
        Set<FoodResponse> actualResponse = foodService.retrieveAllFoods(user);

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(foodRepository).findAllByUserId(1L);
    }

    @Test
    void deleteFoodById_happyFlow() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Food existingFood = new Food();
        existingFood.setUser(user);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        // Act
        foodService.deleteFoodById(user, 99L);

        // Assert
        verify(foodRepository).delete(existingFood);
    }

    @Test
    void deleteFoodById_foodNotFound() {
        // Arrange
        User user = new User();
        when(foodRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> foodService.deleteFoodById(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteFoodById_foodNotOwned() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(2L);

        Food existingFood = new Food();
        existingFood.setUser(owner);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        // Act + Assert
        assertThatThrownBy(() -> foodService.deleteFoodById(user, 99L))
                .isInstanceOf(ResourceNotOwnedException.class);
    }
}