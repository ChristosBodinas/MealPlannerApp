package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodPriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.FoodUnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.IllegalDuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ResourceNotOwnedException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    private void verifyUniqueUnitsAndPrices(Set<FoodUnitRequest> units, Set<FoodPriceRequest> prices) {
        if (units != null && units.size() > units.stream().distinct().count()) {
            throw new IllegalDuplicateValueException("A food can't have duplicates of the same unit.");
        }
        if (prices != null && prices.size() > prices.stream().distinct().count()) {
            throw new IllegalDuplicateValueException("A food can't have duplicates of the same merchant.");
        }
    }

    public Food findAndVerifyFoodEntity(Long userId, Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested food (id: " + foodId + ") was not found."));
        if (!food.getUser().getId().equals(userId)) {
            throw new ResourceNotOwnedException("Requested food (id: " + foodId + ") belongs to another user.");
        }
        return food;
    }

    // createNewFood
    public FoodResponse createFood(User user, FoodRequest request) {
        Food food = foodMapper.createFromRequest(request);
        food.setUser(user);
    }

    // updateFood

    // deleteFood

    // retrieveFood

    // searchFoods
}
