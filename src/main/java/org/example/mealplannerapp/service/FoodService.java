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
        Food food = foodRepository.findByIdVerified(user.getId(), foodId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Requested food (id: " + foodId + ") was not found.")
            );
        return food;
    }

    public FoodResponse createFood(User user, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request.units(), request.prices());

        Food food = foodMapper.createFromRequest(request);
        food.setUser(user);

        Food saved = foodRepository.save(food);
        return foodMapper.generateResponse(food);
    }

    public FoodResponse updateFood(User user, Long foodId, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request.units(), request.prices());

        Food food = findAndVerifyFoodEntity(user.getId(), foodId);
        foodMapper.updateFromRequest(food, request);

        return foodMapper.generateResponse(food);
    }

    // deleteFood
    public void deleteFood(User user, Long foodId) {
        foodRepository.deleteByIdVerified(user.getId(), foodId);
    }

    // retrieveFood
    public FoodResponse retrieveFood(User user, Long foodId) {
        Food food = findAndVerifyFoodEntity(user.getId(), foodId);
        return foodMapper.generateResponse(food);
    }

    // searchFoods
    public List<ListedFoodResponse> searchFoods(User user, String search) {
        return foodRepository.searchByText(user.getId(), search)
            .stream()
            .map(foodMapper::generateResponse)
            .toList();
    }
}
