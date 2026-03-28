package com.atl.backend.repository;

import com.atl.backend.model.FavoriteTournament;
import com.atl.backend.model.FavoriteTournamentId;
import com.atl.backend.model.enums.SportCategory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteTournamentRepository extends JpaRepository<FavoriteTournament, FavoriteTournamentId> {

    @Query("SELECT ft FROM FavoriteTournament ft WHERE ft.account.id = :accountId AND ft.tournament.sport.sportCategory = :sportCategory")
    public List<FavoriteTournament> findByAccount_IdAndTournament_Sport_SportCategory(Integer accountId,
            SportCategory sportCategory);
}
