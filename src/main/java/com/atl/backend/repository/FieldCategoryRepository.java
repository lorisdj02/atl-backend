package com.atl.backend.repository;

import com.atl.backend.model.FieldCategory;
import com.atl.backend.model.FieldCategoryId;
import com.atl.backend.model.enums.SportCategory;

import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldCategoryRepository extends JpaRepository<FieldCategory, FieldCategoryId> {


        FieldCategory findBySportCategory(SportCategory sportCategory);

        @Query("SELECT MAX(fc.field.closingTime) FROM FieldCategory fc WHERE fc.field.city.id = :id AND fc.sportCategory = :sportCategory")
        public LocalTime findMaxClosingTimeByCity_IdAndSportCategory(Integer id, SportCategory sportCategory);

        @Query("SELECT MIN(fc.field.openingTime) FROM FieldCategory fc WHERE fc.field.city.id = :id AND fc.sportCategory = :sportCategory")
        public LocalTime findMinOpeningTimeByCity_IdAndSportCategory(Integer id, SportCategory sportCategory);
}
