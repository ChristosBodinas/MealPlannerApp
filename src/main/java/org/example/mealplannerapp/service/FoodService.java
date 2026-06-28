package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ResourceNotOwnedException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    private Food tryFindById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food with id " + id + " not found."));
    }

    private void verifyOwnership(Long userId, Food food) {
        if (!food.getUser().getId().equals(userId)) {
            throw new ResourceNotOwnedException("Food with id " + food.getId() + " belongs to another user.");
        }
    }

    public FoodResponse createNewFood(User user, FoodRequest request) {
        Food food = foodMapper.createFromRequest(request);
        food.setUser(user);
        return foodMapper.generateResponse(foodRepository.save(food));
    }

    public FoodResponse updateFoodById(User user, Long foodId, FoodRequest request) {
        Food food = tryFindById(foodId);
        verifyOwnership(user.getId(), food);
        foodMapper.updateFromRequest(food, request);
        return foodMapper.generateResponse(foodRepository.save(food));
    }

    public void deleteFoodById(User user, Long foodId) {
        Food food = tryFindById(foodId);
        verifyOwnership(user.getId(), food);
        foodRepository.delete(food);
    }

    public FoodResponse retrieveFoodById(User user, Long foodId) {
        Food food = tryFindById(foodId);
        verifyOwnership(user.getId(), food);
        return foodMapper.generateResponse(food);
    }

    public Set<FoodResponse> retrieveAllFoods(User user) {
        return foodRepository.findAllByUserId(user.getId())
                .stream()
                .map(foodMapper::generateResponse)
                .collect(Collectors.toSet());
    }

    public Set<FoodResponse> retrieveFoodsByTextSearch(User user, String text) {
        return foodRepository.findAllByUserIdAndTextSearch(user.getId(), text)
                .stream()
                .map(foodMapper::generateResponse)
                .collect(Collectors.toSet());
    }
}
