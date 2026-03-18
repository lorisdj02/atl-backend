package com.atl.backend.repository;

import com.atl.backend.model.FootballTeamStat;
import com.atl.backend.model.FootballTeamStatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballTeamStatRepository extends JpaRepository<FootballTeamStat, FootballTeamStatId> {
}