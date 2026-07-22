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
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service that handles create, read, update, and delete operations for {@link Food} entities.
 */
@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    /**
     * <p>Verifies that the submitted {@code units} and {@code prices} have unique unit names and merchants, respectively.
     * </p>
     *
     * @param units  A set of submitted reference unit data.
     * @param prices A set of submitted pricing data.
     * @throws IllegalDuplicateValueException if there are multiple {@code units} with the same name or multiple
     *                                        {@code prices} with the same merchant
     */
    private void verifyUniqueUnitsAndPrices(Set<UnitRequest> units, Set<PriceRequest> prices) {
        if (units != null
                && units.size() > units.stream().map(UnitRequest::name).distinct().count()) {
            throw new IllegalArgumentException("A food can't have duplicates of the same unit.");
        }

        if (prices != null
                && prices.size() > prices.stream().map(PriceRequest::vendor).distinct().count()) {
            throw new IllegalArgumentException("A food can't have duplicates of the same merchant.");
        }
    }

    /**
     * Creates a new {@link Food} entity using data from {@code request} and owned by {@code user},
     * then saves it to the database.
     * @param user the requesting user
     * @param request the submitted food data
     * @return a response containing the full data (units and prices included) of the newly created Food
     * @throws IllegalArgumentException if the submitted data contains duplicate unit or vendor names
     */
    public FoodResponse createFood(User user, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request.units(), request.prices());

        Food food = foodMapper.createFromRequest(request);
        food.setUser(user);

        Food saved = foodRepository.save(food);
        return foodMapper.generateResponse(saved);
    }

    /**
     * <p> Overwrites the data of the {@link Food} entity identified by {@code foodId} and owned by {@code user}
     * with the submitted {@code request} data.
     * </p>
     *
     * @param user    the requesting user
     * @param foodId  the identifier of the requested food
     * @param request the submitted food data
     * @return the full data (incl. units and prices) of the updated food
     * @throws IllegalDuplicateValueException if the submitted data contains multiple units with the same
     *                                        name or multiple prices with the same merchant
     * @throws ResourceNotFoundException      if the requested food isn't found or doesn't belong to {@code user}
     */
    @Transactional
    public FoodResponse updateFood(User user, Long foodId, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request.units(), request.prices());

        Food food = foodRepository.fetchByIdVerified(user.getId(), foodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested food (id: " + foodId + ") not found.")
                );
        foodMapper.updateFromRequest(food, request);

        return foodMapper.generateResponse(food);
    }

    /**
     * <p>Deletes the {@link Food entity} identified by {@code foodId} and owned by {@code user}.
     * </p>
     *
     * @param user   the requesting user
     * @param foodId the identifier of the requested food
     * @throws ResourceNotFoundException if the requested food isn't found or doesn't belong to {@code user}
     */
    @Transactional
    public void deleteFood(User user, Long foodId) {
        // NOTE: The associated FoodPrice and FoodUnit collections are deleted automatically.
        if (foodRepository.deleteByIdVerified(user.getId(), foodId) == 0) {
            throw new ResourceNotFoundException("Requested food (id: " + foodId + ") was not found.");
        }
    }

    /**
     * <p>Retrieves the {@link Food} entity identified by {@code foodId} and owned by {@code user}.
     * </p>
     *
     * @param user   the requesting user
     * @param foodId the identifier of the requested food
     * @return the full data (units and prices included) of the requested food
     * @throws ResourceNotFoundException if the requested food isn't found or doesn't belong to {@code user}
     */
    public FoodResponse retrieveFood(User user, Long foodId) {
        Food food = foodRepository.fetchByIdVerified(user.getId(), foodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested food (id: " + foodId + ") not found.")
                );
        return foodMapper.generateResponse(food);
    }

    /**
     * <p>Retrieves all {@link Food} entities owned by {@code user} and whose name or brand contain
     * {@code search}. If {@code search} is an empty String, the method retrieves all {@link Food} entities
     * owned by {@code user}, regardless of name of brand.
     * </p>
     *
     * @param user   the requesting user
     * @param search the submitted search text
     * @return the identifiers, names, brands, and nutritional data of the matching foods
     */
    public List<ListedFoodResponse> searchFoods(User user, String search) {
        return foodRepository.fetchShallowByTextVerified(user.getId(), search)
                .stream()
                .map(foodMapper::generateListedResponse)
                .toList();
    }
}