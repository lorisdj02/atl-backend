package com.atl.backend.controller;
import java.util.List;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.SportDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.service.SportService;
import com.atl.backend.model.Sport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sports")
public class SportController {

    @Autowired
    private SportService sportService;

    @GetMapping("/{sportCategory}")
    public ResponseEntity<List<SportDTO>> getSportByCategory(@PathVariable String sportCategory) {
        if(!sportService.isSupportedSportCategory(sportCategory)) {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        SportCategory category = SportCategory.valueOf(sportCategory.toUpperCase());
        List<Sport> sports = sportService.getSportsByCategory(category);
        List<SportDTO> sportDTOs = sports.stream().map(SportDTO::fromSport).toList();
        return ResponseEntity.ok(sportDTOs);
    }
}
