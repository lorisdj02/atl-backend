package com.atl.backend.repository;

import com.atl.backend.model.FailedTournamentParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedTournamentParticipationRepository extends JpaRepository<FailedTournamentParticipation, Integer> {
}
