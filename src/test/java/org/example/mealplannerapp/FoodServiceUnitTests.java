package org.example.mealplannerapp;

import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class FoodServiceUnitTests {

    @Mock private FoodRepository foodRepository;
    @Mock private FoodMapper foodMapper;
    
    @InjectMocks
    private FoodService foodService;

    private double purchasePrice = 7.8;
    private double purchaseWeight = 0.4;
    private double edibleRatio = 0.9;

    private FoodRequest defaultFoodRequest() {
        return new FoodRequest(
            "Fake Food", "Fake Brand", // name, brand
            97.0, 12.0, 37.5, 4.5, 6.0, // calories100g, protein100g, carbs100g, fat100g, fiber100g
            7.8, 0.4, 0.9); // purchasePrice, purchaseWeight, edibleRatio
    }

    private FoodResponse defaultFoodResponse() {
        return new FoodResponse(
            1L, "Fake Food", "Fake Brand", // id, name, brand
            97.0, 12.0, 37.5, 4.5, 6.0, // calories100g, protein100g, carbs100g, fat100g, fiber100g
            7.8 / (0.4 * 0.9)); // price100g
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
        assertThrownBy(() -> foodService.updateExistingFood(user, 99L, request))
            .isInstanceOf(ResourceNotFoundException);
    }

    @Test
    void updateExistingFood_foodNotOwned() {
        // Arrange
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        User owner = mock(user.class);
        when(owner.getId().thenReturn(2L));

        Food existingFood = new Food();
        existingFood.setUser(owner);
        when(foodRepository.findById(99L)).thenReturn(Optional.of(existingFood));

        // Act + Assert
        assertThrownBy(() -> foodService.updateExistingFood(user, 99L, ))

    }

}