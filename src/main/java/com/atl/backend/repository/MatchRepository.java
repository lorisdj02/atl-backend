package com.atl.backend.repository;

import com.atl.backend.model.Match;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
    List<Match> findByTournament_IdAndGroupName(Integer tournamentId, String groupName);
}