package com.atl.backend.repository;

import com.atl.backend.model.Callup;
import com.atl.backend.model.CallupId;

import java.util.Optional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallupRepository extends JpaRepository<Callup, CallupId> {
    public Optional<Callup> findByPlayer_PlayerIdAndTournament_Id(Integer playerId, Integer tournamentId);

    public void deleteByPlayer_PlayerIdAndTournament_Id(Integer playerId, Integer tournamentId);

    public List<Callup> findByTournament_Id(Integer tournamentId);

    public List<Callup> findByPlayer_PlayerId(Integer playerId);

    public List<Callup> findByTournament_IdAndTeam_Id(Integer tournamentId, Integer teamId);

    public void deleteByTeam_IdAndTournament_Id(Integer teamId, Integer tournamentId);
}