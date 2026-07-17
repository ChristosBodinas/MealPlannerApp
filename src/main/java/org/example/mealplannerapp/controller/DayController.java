package org.example.mealplannerapp.controller;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.service.DayService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class DayController {

    private final DayService dayService;
}
