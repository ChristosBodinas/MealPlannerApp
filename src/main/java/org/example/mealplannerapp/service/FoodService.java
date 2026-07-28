package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service that handles create, update, delete, retrieve, and text search
 * operations for {@link Food} entities.
 */
@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    private void verifyUniqueUnitsAndPrices(FoodRequest request) {
        Set<UnitRequest> units = request.units();
        Set<PriceRequest> prices = request.prices();

        if (units != null
                && units.size() > units.stream().map(UnitRequest::name).distinct().count()) {
            throw new ServiceValidationException("A food can't have duplicates of the same unit.");
        }

        if (prices != null
                && prices.size() > prices.stream().map(PriceRequest::vendor).distinct().count()) {
            throw new ServiceValidationException("A food can't have duplicates of the same merchant.");
        }
    }

    /**
     * Creates a new {@link Food} entity using data from {@code request} and owned by {@code user},
     * then saves it to the database
     *
     * @param user    the user making the request
     * @param request the submitted food data
     * @return a response containing the new food's full data (units and prices included)
     * @throws ServiceValidationErrorException if the submitted data contains duplicate unit or vendor names
     */
    public FoodResponse createFood(User user, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request);

        Food food = foodMapper.createFromRequest(request);
        food.setUser(user);

        Food saved = foodRepository.save(food);
        return foodMapper.generateResponse(saved);
    }

    /**
     * Finds the {@link Food} entity identified by {@code foodId} and owned by {@code user}, and overwrites
     * its data with the submitted {@code request} data.
     *
     * @param user    the user making the request
     * @param foodId  the identifier of the food to be updated
     * @param request the submitted food data
     * @return a response containing the updated food's full data (units and prices included)
     * @throws ResourceNotFoundException       if the food does not exist or belongs to another user
     * @throws ServiceValidationErrorException if the submitted data contains duplicate unit or vendor names
     */
    @Transactional
    public FoodResponse updateFood(User user, Long foodId, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request);

        Food food = foodRepository.fetchByIdVerified(user.getId(), foodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested food (id: " + foodId + ") not found.")
                );
        foodMapper.updateFromRequest(food, request);

        return foodMapper.generateResponse(food);
    }

    /**
     * Deletes the {@link Food} entity identified by {@code foodId} and owned by {@code user}
     * from the database, along with its associated units and prices.
     *
     * @param user   the user making the request
     * @param foodId the identifier of the food to be deleted
     * @throws ResourceNotFoundException if the food does not exist or belongs to another user
     */
    @Transactional
    public void deleteFood(User user, Long foodId) {
        // NOTE: The associated FoodPrice and FoodUnit collections are deleted automatically.
        if (foodRepository.deleteByIdVerified(user.getId(), foodId) == 0) {
            throw new ResourceNotFoundException("Requested food (id: " + foodId + ") was not found.");
        }
    }

    /**
     * Retrieves the full data of the {@link Food} entity identified by {@code foodId} and owned by
     * {@code user}, including its associated units and prices.
     *
     * @param user   the user making the request
     * @param foodId the identifier of the food to be retrieved
     * @return a response containing the requested food's full data (units and prices included)
     * @throws ResourceNotFoundException if the food does not exist or belongs to another user
     */
    public FoodResponse retrieveFood(User user, Long foodId) {
        Food food = foodRepository.fetchByIdVerified(user.getId(), foodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested food (id: " + foodId + ") not found.")
                );
        return foodMapper.generateResponse(food);
    }

    /**
     * Retrieves all {@link Food} entities owned by {@code user} that contain {@code search} in their
     * names or brands. If {@code search} is an empty string, retrieves all {@link Food} entities owned
     * by {@code user}, regardless of their names or brands.
     *
     * @param user   the user making the request
     * @param search the submitted search text
     * @return the core data (units and prices excluded) of all the matching foods
     */
    public List<ListedFoodResponse> searchFoods(User user, String search) {
        return foodRepository.fetchShallowByUserAndText(user.getId(), search)
                .stream()
                .map(foodMapper::generateListedResponse)
                .toList();
    }
}