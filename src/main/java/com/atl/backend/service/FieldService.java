package com.atl.backend.service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.City;
import com.atl.backend.model.Field;
import com.atl.backend.model.FieldCategory;
import com.atl.backend.model.enums.SportCategory;

import java.util.Optional;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.repository.CityRepository;
import com.atl.backend.repository.FieldCategoryRepository;
import com.atl.backend.repository.FieldRepository;
import com.atl.backend.response.FieldsAndMaxPlayersPerFieldResponse;

@Service
public class FieldService {
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private FieldCategoryRepository fieldCategoryRepository;

    public Field saveField(Field field, Map<SportCategory, Short> sportCategories) {
        Field savedField = fieldRepository.save(field);
        for (SportCategory sportCategory : sportCategories.keySet()) {
            FieldCategory fieldCategory = new FieldCategory();
            fieldCategory.setField(savedField);
            fieldCategory.setSportCategory(sportCategory);
            fieldCategory.setMaxPlayers(sportCategories.get(sportCategory));
            fieldCategoryRepository.save(fieldCategory);
        }
        return savedField;
    }

    public City saveCity(City city) {
        return cityRepository.save(city);
    }

    public FieldCategory saveFieldCategory(FieldCategory fieldCategory) {
        return fieldCategoryRepository.save(fieldCategory);
    }

    public Optional<Field> getFieldById(Integer id) {
        return fieldRepository.findById(id);
    }

    public Optional<City> getCityById(Integer id) {
        return cityRepository.findById(id);
    }

    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public void deleteFieldById(Integer id) {
        fieldRepository.deleteById(id);
    }

    public void deleteAllFields() {
        fieldCategoryRepository.deleteAll();
        fieldRepository.deleteAll();
    }

    public List<Field> getFieldsByOrganizationId(Integer organizationId) {
        return fieldRepository.findByOrganization_OrganizationId(organizationId);
    }

    public LocalTime getMinOpeningTimeByCityId(Integer cityId, SportCategory sportCategory) {
        return fieldCategoryRepository.findMinOpeningTimeByCity_IdAndSportCategory(cityId, sportCategory);
    }

    public LocalTime getMaxClosingTimeByCityId(Integer cityId, SportCategory sportCategory) {
        return fieldCategoryRepository.findMaxClosingTimeByCity_IdAndSportCategory(cityId, sportCategory);
    }

    public FieldsAndMaxPlayersPerFieldResponse getFieldsByMaxPlayersAndCityAndSportCategory(Integer maxPlayers,
            Integer cityId, SportCategory sportCategory) {
                List<FieldCategory> fieldCategories = fieldCategoryRepository.findByMaxPlayersAndField_City_IdAndSportCategory(maxPlayers, cityId, sportCategory);
                Map<Integer, Short> maxPlayersPerField = new HashMap<>();
                List<Field> fields = fieldCategories.stream().map(FieldCategory::getField).toList();
                for (FieldCategory field : fieldCategories) {
                    maxPlayersPerField.put(field.getField().getId(), field.getMaxPlayers());
                }
                return FieldsAndMaxPlayersPerFieldResponse.builder()
                        .fields(fields.stream().map(FieldDTO::fromField).toList())
                        .maxPlayersPerField(maxPlayersPerField)
                        .build();
    }

    public List<Field> getFieldsByCityId(Integer cityId) {
        return fieldRepository.findByCity_Id(cityId);
    }

    public Short getMaxPlayersByFieldIdAndSportCategory(Integer fieldId, SportCategory sportCategory) {
        FieldCategory fieldCategory = fieldCategoryRepository.findByField_IdAndSportCategory(fieldId, sportCategory);
        if (fieldCategory == null) {
            return 0;
        }
        return fieldCategory.getMaxPlayers();
    }

    public void checkFieldExists(Integer fieldId) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new NotFoundException(ErrorsString.FIELD_NOT_FOUND);
        }
    }

    public void checkCityExists(Integer cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new NotFoundException(ErrorsString.CITY_NOT_FOUND);
        }
    }
}
