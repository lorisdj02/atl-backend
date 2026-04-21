package com.atl.backend.repository;

import com.atl.backend.model.Match;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournament_IdAndGroupName(Long tournamentId, String groupName);

    List<Match> findByTournament_Id(Long tournamentId);

    List<Match> findByFinalStageBracket_Id(Long finalStageBracketId);
}