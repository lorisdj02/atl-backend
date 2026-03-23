package com.atl.backend.service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atl.backend.dto.CityDTO;
import com.atl.backend.model.City;
import com.atl.backend.response.BookFieldResponse;

import org.springframework.stereotype.Service;

@Service
public class BookingScreenService {
    private final BookingService bookingService;
    private final FieldService fieldService;


    public BookingScreenService(BookingService bookingService, FieldService fieldService) {
        this.bookingService = bookingService;
        this.fieldService = fieldService;
    }

    public BookFieldResponse getBookFieldResponse(){
        Map<Integer, LocalTime> minOpeningTimePerCity = new HashMap<>();
        Map<Integer, LocalTime> maxClosingTimePerCity = new HashMap<>();
        List<City> cities = fieldService.getAllCities();
        for (City city : cities) {
            minOpeningTimePerCity.put(city.getId(), fieldService.getMinOpeningTimeByCityId(city.getId()));
            maxClosingTimePerCity.put(city.getId(), fieldService.getMaxClosingTimeByCityId(city.getId()));
        }
        return BookFieldResponse.builder()
                .cities(cities.stream().map(CityDTO::fromCity).toList())
                .minOpeningTimePerCity(minOpeningTimePerCity)
                .maxClosingTimePerCity(maxClosingTimePerCity)
                .build();
    }
}
