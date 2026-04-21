package com.atl.backend.repository;

import com.atl.backend.model.TeamRequest;
import com.atl.backend.model.TeamRequestId;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRequestRepository extends JpaRepository<TeamRequest, TeamRequestId> {

    Optional<TeamRequest> findByPlayer_PlayerIdAndTeam_Id(Long playerId, Long teamId);

    void deleteByPlayer_PlayerId(Long playerId);

    List<TeamRequest> findByTeam_Id(Long teamId);

    Optional<TeamRequest> findByTeam_IdAndPlayer_PlayerId(Long teamId, Long playerId);
}
