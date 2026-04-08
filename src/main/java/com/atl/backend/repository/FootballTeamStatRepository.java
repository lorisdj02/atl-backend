package com.atl.backend.repository;

import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.FootballTeamStatId;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballTeamStatRepository extends JpaRepository<FootballTeamStat, FootballTeamStatId> {

    List<FootballTeamStat> findByTeam_Id(Integer teamId);

    List<FootballTeamStat> findByPlayer_PlayerId(Integer playerId);

    Optional<FootballTeamStat> findByTeam_IdAndPlayer_PlayerId(Integer teamId, Integer playerId);
}