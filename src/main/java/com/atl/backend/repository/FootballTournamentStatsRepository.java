package com.atl.backend.repository;

import com.atl.backend.model.FootballTournamentStats;
import com.atl.backend.model.FootballTournamentStatsId;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballTournamentStatsRepository extends JpaRepository<FootballTournamentStats, FootballTournamentStatsId> {

    List<FootballTournamentStats> findByTournament_Id(Integer tournamentId);

    Optional<FootballTournamentStats> findByTournament_IdAndPlayer_PlayerIdAndTeam_Id(Integer tournamentId, Integer playerId, Integer teamId);
}