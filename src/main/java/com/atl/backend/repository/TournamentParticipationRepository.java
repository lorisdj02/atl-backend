package com.atl.backend.repository;

import com.atl.backend.model.TournamentParticipation;
import com.atl.backend.model.TournamentParticipationId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentParticipationRepository extends JpaRepository<TournamentParticipation, TournamentParticipationId> {

    List<TournamentParticipation> findByTeam_Id(Integer teamId);

    List<TournamentParticipation> findByTournament_Id(Integer tournamentId);

    Optional<TournamentParticipation> findByTeam_IdAndTournament_Id(Integer teamId, Integer tournamentId);

    List<TournamentParticipation> findByTeam_IdAndToPay(Integer teamId, BigDecimal toPay);

    List<TournamentParticipation> findByTeam_IdAndToPayGreaterThan(Integer teamId, BigDecimal toPay);
}