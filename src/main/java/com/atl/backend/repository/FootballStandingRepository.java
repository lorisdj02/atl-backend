package com.atl.backend.repository;

import java.util.List;
import java.util.Optional;

import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.FootballStandingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballStandingRepository extends JpaRepository<FootballStanding, FootballStandingId> {

    // Custom query method to find standings by tournament ID
    List<FootballStanding> findByTournament_Id(Integer tournamentId);

    // Custom query method to find standings by tournament ID and group name
    List<FootballStanding> findByTournament_IdAndGroupName(Integer tournamentId, String groupName);

    Optional<FootballStanding> findByTournament_IdAndTeam_Id(Integer tournamentId, Integer teamId);
}