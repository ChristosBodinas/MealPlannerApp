package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.FoodRequest;
import org.example.mealplannerapp.dto.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ResourceNotOwnedException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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

    private final double purchasePrice = 7.8;
    private final double purchaseWeight = 0.4;
    private final double edibleRatio = 0.9;

    private FoodRequest defaultFoodRequest() {
        return new FoodRequest(
            "Fake Food", "Fake Brand", // name, brand
            97.0, 12.0, 37.5, 4.5, 6.0, // calories100g, protein100g, carbs100g, fat100g, fiber100g
            purchasePrice, purchaseWeight, edibleRatio);
    }

    private FoodResponse defaultFoodResponse() {
        return new FoodResponse(
            1L, "Fake Food", "Fake Brand", // id, name, brand
            97.0, 12.0, 37.5, 4.5, 6.0, // calories100g, protein100g, carbs100g, fat100g, fiber100g
            purchasePrice / (purchaseWeight * edibleRatio)); // price100g
    }

    private FoodResponse alternateFoodResponse() {
        return new FoodResponse(
            2L, "Made-Up Meal", "Fake Brand", // id, name, brand
            97.0, 12.0, 37.5, 4.5, 6.0, // calories100g, protein100g, carbs100g, fat100g, fiber100g
            purchasePrice / (purchaseWeight * edibleRatio)); // price100g
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
        assertThat(mappedFood.getPrice100g()).isEqualTo(
            request.purchasePrice() / (request.purchaseWeight() * request.edibleRatio()));
    }

    @Test
    void updateExistingFood_happyFlow() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Food existingFood = new Food();
        existingFood.setUser(user);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        FoodRequest request = defaultFoodRequest();

        Food savedFood = new Food();
        when(foodRepository.save(existingFood)).thenReturn(savedFood);

        FoodResponse expectedResponse = defaultFoodResponse();
        when(foodMapper.generateResponse(savedFood)).thenReturn(expectedResponse);

        // Act
        FoodResponse actualResponse = foodService.updateExistingFood(user, 99L, request);

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(existingFood.getPrice100g()).isEqualTo(
            request.purchasePrice() / (request.purchaseWeight() * request.edibleRatio()));
    }

    @Test
    void updateExistingFood_foodNotFound() {
        // Arrange
        User user = new User();
        FoodRequest request = defaultFoodRequest();

        when(foodRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> foodService.updateExistingFood(user, 99L, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateExistingFood_foodNotOwned() {
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
        assertThatThrownBy(() -> foodService.updateExistingFood(user, 99L, request))
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

        when(foodRepository.findAllByUserAndTextSearch(1L, "text"))
                .thenReturn(List.of(food1, food2));
        when(foodMapper.generateResponse(food1)).thenReturn(response1);
        when(foodMapper.generateResponse(food2)).thenReturn(response2);

        List<FoodResponse> expectedResponse = List.of(response1, response2);

        // Act
        List<FoodResponse> actualResponse = foodService.retrieveFoodsByTextSearch(user, "text");

        // Assert
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(foodRepository).findAllByUserAndTextSearch(1L, "text");
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

        when(foodRepository.findAllByUserId(1L)).thenReturn(List.of(food1, food2));
        when(foodMapper.generateResponse(food1)).thenReturn(response1);
        when(foodMapper.generateResponse(food2)).thenReturn(response2);

        List<FoodResponse> expectedResponse = List.of(response1, response2);

        // Act
        List<FoodResponse> actualResponse = foodService.retrieveAllFoods(user);

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