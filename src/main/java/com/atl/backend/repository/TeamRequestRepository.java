package com.atl.backend.repository;

import com.atl.backend.model.TeamRequest;
import com.atl.backend.model.TeamRequestId;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRequestRepository extends JpaRepository<TeamRequest, TeamRequestId> {

    Optional<TeamRequest> findByPlayer_PlayerIdAndTeam_Id(Integer playerId, Integer teamId);

    void deleteByPlayer_PlayerId(Integer playerId);
}
