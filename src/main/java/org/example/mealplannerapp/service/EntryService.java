package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;

    // createNewEntry

    // retrievePartialEntry -> for aggregate purposes; does not join with food, food_unit, and food_price

    // retrieveCompleteEntry -> for displaying the day's entries

    // deleteEntryById

    // deleteEntriesByDayId

    // reorderEntries

    // duplicateEntry
}
