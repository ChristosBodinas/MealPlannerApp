package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.ExerciseEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.Placement;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.ExerciseRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that handles create, duplicate, edit, move, delete, and retrieve
 * operations on individual {@link Entry} entities.
 */
@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final DayRepository dayRepository;
    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;
    private final EntryMapper entryMapper;

    /**
     * Creates a new {@link Entry} entity using data from {@code request} and owned by {@code user},
     * places it in the {@link Day} with identifier {@code dayId}, then saves it to the database.
     *
     * @param user    the user making the request
     * @param dayId   the identifier of the day in which to place the entry
     * @param request the submitted entry data
     * @return a response containing the full data of the new entry and its referenced item (food, exercise, or meal)
     * @throws ResourceNotFoundException if the given day or referenced item does not exist or belongs to another user
     */
    public EntryResponse createEntry(User user, Long dayId, EntryCreateRequest request) {
        Day day = dayRepository.fetchByIdVerified(user.getId(), dayId)  // 
                .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        Entry entry = entryMapper.toEntry(request);
        entry.setDay(day);

        int count = entryRepository.countByDayAndCategory(dayId, request.category());
        entry.setPosition(count + 1);

        switch (request) {
            case FoodEntryCreateRequest f:
                Food food = foodRepository.fetchByIdVerified(user.getId(), f.foodId())
                        .orElseThrow(() -> new ResourceNotFoundException("Requested food (id: " + f.foodId() + ") not found."));
                ((FoodEntry) entry).setFood(food);
                break;
            case ExerciseEntryCreateRequest x:
                Exercise exercise = exerciseRepository.fetchByIdVerified(user.getId(), x.exerciseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Requested exercise (id: " + x.exerciseId() + ") not found."));
                ((ExerciseEntry) entry).setExercise(exercise);
                break;
        }

        entry.snapshotNutritionAndPriceInfo();

        Entry saved = entryRepository.save(entry);
        return entryMapper.toResponse(saved);
    }

    /**
     * Creates a new {@link Entry} by duplicating an existing one identified in {@code request}, places
     * it in the {@link Day} with identifier {@code dayId} and the category identified in {@code request},
     * then saves it to the database.
     * <p>The new entry's snapshot values are calculated from scratch, so they might differ from the original's
     * if the latter are outdated.</p>
     *
     * @param user    the user making the request
     * @param dayId   the identifier of the day in which to place the entry
     * @param request the submitted duplication data
     * @return a response containing the full data of the new entry and its referenced item (food, exercise, or meal)
     * @throws ResourceNotFoundException if the given day or entry does not exist or belongs to another user
     */
    public EntryResponse duplicateEntry(User user, Long dayId, EntryDuplicateRequest request) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), request.entryId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + request.entryId() + ") not found."));

        Day day = dayRepository.fetchByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + dayId + ") not found."));

        Entry copy = entry.createDuplicate();
        copy.setDay(day);
        copy.setCategory(request.category());

        int count = entryRepository.countByDayAndCategory(dayId, request.category());
        copy.setPosition(count + 1);

        Entry saved = entryRepository.save(copy);
        return entryMapper.toResponse(saved);
    }

    /**
     * Finds the {@link Entry} entity identified by {@code entryId} and owned by {@code user}, and updates its
     * quantity and display parameters, then recalculates its snapshot values to account for the change.
     *
     * @param user    the user making the request
     * @param entryId the identifier of the entry to be edited
     * @param request the submitted entry data
     * @return a response containing the full data of the edited entry and its referenced item (food, exercise, or meal)
     * @throws ResourceNotFoundException if the entry does not exist or belongs to another user
     */
    @Transactional
    public EntryResponse editEntry(User user, Long entryId, EntryEditRequest request) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        entryMapper.update(entry, request);
        entry.snapshotNutritionAndPriceInfo();

        return entryMapper.toResponse(entry);
    }

    /**
     * Finds the {@link Entry} entity identified by {@code entryId} and owned by {@code user}, and updates its
     * category and position according to {@code request}. Other entries in the initial and final category also
     * have their positions changed to close gaps and make room respectively.
     *
     * @param user    the user making the request
     * @param entryId the identifier of the entry to be moved
     * @param request the submitted movement data
     * @throws ResourceNotFoundException  if the entry does not exist or belongs to another user
     * @throws ServiceValidationException if the desired position is out of bounds for the target category
     */
    @Transactional
    public void moveEntry(User user, Long entryId, EntryMoveRequest request) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        Long dayId = entry.getDay().getId();
        int currentPosition = entry.getPosition();
        int desiredPosition = request.desiredPosition();

        if (entry.getCategory() == request.category() && currentPosition == desiredPosition) {
            throw new ServiceValidationException("Requested entry is already in the desired category and position.");
        }

        int count = entryRepository.countByDayAndCategory(dayId, request.category());

        if (entry.getCategory() == request.category()) {
            if (desiredPosition > count) {
                throw new ServiceValidationException("Desired position is out of bounds.");
            }

            if (currentPosition > desiredPosition) {
                entryRepository.shiftUpByDayAndCategory(dayId, request.category(), desiredPosition, currentPosition);
            } else if (currentPosition < desiredPosition) {
                entryRepository.shiftDownByDayAndCategory(dayId, request.category(), currentPosition, desiredPosition);
            }

            entry.setPosition(desiredPosition);

        } else {
            if (desiredPosition > count + 1) {
                throw new ServiceValidationException("Desired position is out of bounds.");
            }

            entryRepository.shiftUpByDayAndCategory(dayId, request.category(), desiredPosition, null);
            entryRepository.shiftDownByDayAndCategory(dayId, entry.getCategory(), currentPosition, null);

            entry.setCategory(request.category());
            entry.setPosition(desiredPosition);
        }
    }

    /**
     * Finds the {@link Entry} entity identified by {@code entryId} and owned by {@code user}, and deletes it
     * from the database. Afterwards, moves the other entries in the same {@link Day} and category to close the gap.
     *
     * @param user    the user making the request
     * @param entryId the identifier of the entry to be deleted
     * @throws ResourceNotFoundException if the entry does not exist or belongs to another user
     */
    @Transactional
    public void deleteEntry(User user, Long entryId) {
        Placement placement = entryRepository.extractPlacementByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        entryRepository.deleteByIdVerified(user.getId(), entryId);

        entryRepository.shiftDownByDayAndCategory(
                placement.getDayId(),
                placement.getCategory(),
                placement.getPosition(),
                null);
    }

    /**
     * Retrieves the full data of the {@link Entry} entity identified by {@code entryId} and owned by
     * {@code user}, including its associated units and prices.
     *
     * @param user    the user making the request
     * @param entryId the identifier of the entry to be retrieved
     * @return a response containing the full data of the requested entry and its referenced item (food, exercise, or meal)
     * @throws ResourceNotFoundException if the entry does not exist or belongs to another user
     */
    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        return entryMapper.toResponse(entry);
    }

}