package com.atl.backend.repository;

import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.SportCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    
    public List<Tournament> findBySport_SportCategory(SportCategory sportCategory);
}