package com.atl.backend.controller;
import java.util.Optional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.ErrorResponse;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.model.Sport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sports")
public class SportController {
    
}
