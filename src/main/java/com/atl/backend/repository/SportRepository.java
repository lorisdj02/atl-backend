package com.atl.backend.repository;

import com.atl.backend.model.Sport;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {

    Optional<Sport> findByName(String name);
}