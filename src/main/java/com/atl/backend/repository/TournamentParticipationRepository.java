package com.atl.backend.repository;

import com.atl.backend.model.TournamentParticipation;
import com.atl.backend.model.TournamentParticipationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentParticipationRepository extends JpaRepository<TournamentParticipation, TournamentParticipationId> {
}