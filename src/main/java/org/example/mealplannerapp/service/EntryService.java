package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.request.DuplicateEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateExerciseEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateFoodEntryRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EditEntryRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EditExerciseEntryRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EditFoodEntryRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.InvalidReferenceException;
import org.example.mealplannerapp.exception.MappingMismatchException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.Placement;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.ExerciseRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final DayRepository dayRepository;
    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;
    private final EntryMapper entryMapper;

    // TODO: Javadocs.


    //<editor-fold desc="VALIDATION METHODS">
    private void throwIfInvalidUnit(String unitName, Set<ReferenceUnit> units) {
        List<String> unitNames = units.stream()
                .map(ReferenceUnit::getName)
                .toList();

        if (unitName != null && !unitNames.contains(unitName)) {
            throw new InvalidReferenceException("Invalid reference unit name submitted.");
        }
    }

    private void throwIfInvalidVendor(String vendorName, Set<VendorData> vendors) {
        List<String> vendorNames = vendors.stream()
                .map(VendorData::getName)
                .toList();

        if (vendorName != null && !vendorNames.contains(vendorName)) {
            throw new InvalidReferenceException("Invalid reference unit name submitted.");
        }
    }

    private void throwIfInvalidLevel(String levelName, Set<EffortLevel> levels) {
        List<String> levelNames = levels.stream()
                .map(EffortLevel::getName)
                .toList();

        if (levelName != null && !levelNames.contains(levelName)) {
            throw new InvalidReferenceException("Invalid effort level name submitted.");
        }
    }

    private void throwIfDeletedReference(Entry entry) {
        if (entry instanceof FoodEntry f && f.getFood() == null) {
            throw new InvalidReferenceException(
                    "The selected entry references a deleted food. " +
                            "Requested operation not allowed.");
        }

        if (entry instanceof ExerciseEntry x && x.getExercise() == null) {
            throw new InvalidReferenceException(
                    "The selected entry references a deleted exercise. " +
                            "Requested operation not allowed.");
        }
    }
    //</editor-fold>

    //<editor-fold desc="CREATION SUB-METHODS"
    private FoodEntry createFoodEntry(
            Long userId, CreateFoodEntryRequest request
    ) {
        Food food = foodRepository.fetchByIdVerified(userId, request.foodId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested food (id: " + request.foodId() + ") not found."));

        throwIfInvalidUnit(request.unitName(), food.getUnits());
        throwIfInvalidVendor(request.vendorName(), food.getVendors());

        BigDecimal grams;
        BigDecimal unitQuantity;

        if (request.unitName() == null) {
            // If no unit name is submitted, then the submitted quantity is interpreted as grams.
            unitQuantity = null;
            grams = request.quantity();
        } else {
            // If a unit name is submitted, then the submitted quantity is interpreted as referring to that unit
            // and grams are calculated separately.
            unitQuantity = request.quantity();
            grams = unitQuantity.multiply(food.mapUnitsToGrams().get(request.unitName()));
        }

        return FoodEntry.builder()
                .food(food)
                .unitName(request.unitName())
                .vendorName(request.vendorName())
                .grams(grams).unitQuantity(unitQuantity)
                .build();
    }

    private ExerciseEntry createExerciseEntry(
            Long userId, CreateExerciseEntryRequest request
    ) {
        Exercise exercise = exerciseRepository.fetchByIdVerified(userId, request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested exercise (id: " + request.exerciseId() + ") not found."));

        throwIfInvalidLevel(request.levelName(), exercise.getLevels());

        return ExerciseEntry.builder()
                .exercise(exercise)
                .levelName(request.levelName())
                .duration(request.duration())
                .build();
    }
    //</editor-fold>

    private void editFoodEntry(
            FoodEntry entry, EditFoodEntryRequest request
    ) {
        throwIfInvalidUnit(request.unitName(), entry.getFood().getUnits());
        throwIfInvalidVendor(request.vendorName(), entry.getFood().getVendors());

        entry.setUnitName(request.unitName());
        entry.setVendorName(request.vendorName());

        if (request.unitName() == null) {
            entry.setUnitQuantity(null);
            entry.setGrams(request.quantity());
        } else {
            entry.setUnitQuantity(request.quantity());
            entry.setGrams(request.quantity()
                    .multiply(entry.getFood().mapUnitsToGrams().get(request.unitName())));
        }
    }

    private void editExerciseEntry(
            ExerciseEntry entry, EditExerciseEntryRequest request
    ) {
        throwIfInvalidLevel(request.levelName(), entry.getExercise().getLevels());

        entry.setDuration(request.duration());
        entry.setLevelName(request.levelName());
    }

    public EntryResponse createEntry(
            User user, Long dayId, CreateEntryRequest request
    ) {
        Long userId = user.getId();

        Day day = dayRepository.fetchByIdVerified(userId, dayId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested day (id: " + dayId + ") not found."));

        Entry entry = switch (request) {
            case CreateFoodEntryRequest f -> createFoodEntry(userId, f);
            case CreateExerciseEntryRequest x -> createExerciseEntry(userId, x);
        };

        int count = entryRepository.countByDayAndCategory(dayId, request.category());

        entry.snapshotInfo();

        entry.setDay(day);
        entry.setCategory(request.category());
        entry.setPosition(count + 1);

        Entry saved = entryRepository.save(entry);
        return entryMapper.toResponse(saved);
    }

    public EntryResponse duplicateEntry(
            User user, Long dayId, DuplicateEntryRequest request
    ) {
        Long userId = user.getId();

        Day day = dayRepository.fetchByIdVerified(userId, dayId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested day (id: " + dayId + ") not found."));

        Entry source = entryRepository.fetchByIdVerified(userId, request.entryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + request.entryId() + ") not found."));

        // Abort the duplication if the source entry's referenced food/exercise no longer exists.
        throwIfDeletedReference(source);

        Entry copy = source.createDuplicate();

        int count = entryRepository.countByDayAndCategory(dayId, request.category());
        copy.setDay(day);
        copy.setCategory(request.category());
        copy.setPosition(count + 1);

        Entry saved = entryRepository.save(copy);
        return entryMapper.toResponse(saved);
    }

    @Transactional
    public EntryResponse editEntry(
            User user, Long entryId, EditEntryRequest request
    ) {
        Long userId = user.getId();

        Entry entry = entryRepository.fetchByIdVerified(userId, entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + entryId + ") not found."));

        // Abort the duplication if the requested entry's referenced food/exercise no longer exists.
        throwIfDeletedReference(entry);

        // TODO: STUFF
        if (entry instanceof FoodEntry fe && request instanceof EditFoodEntryRequest fr) {
            editFoodEntry(fe, fr);
        } else if (entry instanceof ExerciseEntry xe && request instanceof EditExerciseEntryRequest xr) {
            editExerciseEntry(xe, xr);
        } else {
            throw new MappingMismatchException("Submitted data type does not match requested entry type.");
        }

        entry.snapshotInfo();
        return entryMapper.toResponse(entry);
    }

    @Transactional
    public void deleteEntry(
            User user, Long entryId
    ) {
        Long userId = user.getId();

        Placement placement = entryRepository.extractPlacementByIdVerified(userId, entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + entryId + ") not found."));

        // TODO: Explain lack of verification?
        entryRepository.deleteById(entryId);

        entryRepository.closeGapAfterRemoval(
                placement.dayId(),
                placement.category(),
                placement.position());
    }

    @Transactional(readOnly = true)
    public EntryResponse retrieveEntry(
            User user, Long entryId
    ) {
        Long userId = user.getId();
        Entry entry = entryRepository.fetchByIdVerified(userId, entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + entryId + ") not found."));

        return entryMapper.toResponse(entry);
    }

}
