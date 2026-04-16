package com.atl.backend.repository;

import com.atl.backend.model.FootballTournamentStat;
import com.atl.backend.model.FootballTournamentStatsId;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballTournamentStatRepository extends JpaRepository<FootballTournamentStat, FootballTournamentStatsId> {

    List<FootballTournamentStat> findByTournament_Id(Integer tournamentId);

    Optional<FootballTournamentStat> findByTournament_IdAndPlayer_PlayerIdAndTeam_Id(Integer tournamentId, Integer playerId, Integer teamId);
}