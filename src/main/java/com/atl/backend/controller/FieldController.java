package com.atl.backend.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atl.backend.dto.CityDTO;
import com.atl.backend.service.FieldService;

@RestController
@RequestMapping("/api/fields")
public class FieldController {
    @Autowired
    private FieldService fieldService;

    @GetMapping("/all-cities")
    public ResponseEntity<List<CityDTO>> getAllCities() {
        List<CityDTO> cities = fieldService.getAllCities().stream()
                .map(city -> CityDTO.fromCity(city))
                .toList();
        return ResponseEntity.ok(cities);
    }
}
