package com.atl.backend.service;

import com.atl.backend.model.City;
import com.atl.backend.model.Field;
import com.atl.backend.model.FieldCategory;
import com.atl.backend.model.enums.SportCategory;

import java.util.Optional;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.atl.backend.repository.CityRepository;
import com.atl.backend.repository.FieldCategoryRepository;
import com.atl.backend.repository.FieldRepository;

@Service
public class FieldService {
    final FieldRepository fieldRepository;
    final CityRepository cityRepository;
    final FieldCategoryRepository fieldCategoryRepository;

    public FieldService(FieldRepository fieldRepository, CityRepository cityRepository,
            FieldCategoryRepository fieldCategoryRepository) {
        this.fieldRepository = fieldRepository;
        this.cityRepository = cityRepository;
        this.fieldCategoryRepository = fieldCategoryRepository;
    }

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

    public Optional<Field> getFieldById(Integer id) {
        return fieldRepository.findById(id);
    }

    public Optional<City> getCityById(Integer id) {
        return cityRepository.findById(id);
    }

    public FieldCategory saveFieldCategory(FieldCategory fieldCategory) {
        return fieldCategoryRepository.save(fieldCategory);
    }

    public List<Field> getAllFields() {
        return fieldRepository.findAll();
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
}
