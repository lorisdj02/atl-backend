package com.atl.backend.repository;

import com.atl.backend.model.TeamRequest;
import com.atl.backend.model.TeamRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRequestRepository extends JpaRepository<TeamRequest, TeamRequestId> {
}
