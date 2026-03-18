package com.atl.backend.repository;

import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.FootballStandingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballStandingRepository extends JpaRepository<FootballStanding, FootballStandingId> {
}