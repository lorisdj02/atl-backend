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

    List<TournamentParticipation> findByTeam_Id(Long teamId);

    List<TournamentParticipation> findByTournament_Id(Long tournamentId);

    List<TournamentParticipation> findByTournament_IdAndToPayGreaterThan(Long tournamentId, BigDecimal toPay);

    List<TournamentParticipation> findByTournament_IdAndToPay(Long tournamentId, BigDecimal toPay);

    Optional<TournamentParticipation> findByTeam_IdAndTournament_Id(Long teamId, Long tournamentId);

    List<TournamentParticipation> findByTeam_IdAndToPay(Long teamId, BigDecimal toPay);

    List<TournamentParticipation> findByTeam_IdAndToPayGreaterThan(Long teamId, BigDecimal toPay);

    void deleteByTeam_IdAndTournament_Id(Long teamId, Long tournamentId);
}