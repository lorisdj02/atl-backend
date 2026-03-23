package com.atl.backend.repository;

import com.atl.backend.model.City;

import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {

    @Query("SELECT MAX(f.closingTime) FROM Field f WHERE f.city.id = :id")
    public LocalTime findMaxClosingTimeByCity_Id(Integer id);

    @Query("SELECT MIN(f.openingTime) FROM Field f WHERE f.city.id = :id")
    public LocalTime findMinOpeningTimeByCity_Id(Integer id);
}