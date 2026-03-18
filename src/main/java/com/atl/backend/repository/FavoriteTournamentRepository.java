package com.atl.backend.repository;

import com.atl.backend.model.FavoriteTournament;
import com.atl.backend.model.FavoriteTournamentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteTournamentRepository extends JpaRepository<FavoriteTournament, FavoriteTournamentId> {
}
