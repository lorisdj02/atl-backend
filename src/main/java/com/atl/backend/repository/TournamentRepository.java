package com.atl.backend.repository;

import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.SportCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {

    public List<Tournament> findBySport_SportCategory(SportCategory sportCategory);

    @Query("SELECT t FROM Tournament t WHERE t.status <> 'CANCELED' AND t.status <> 'FINISHED' AND t.status <> 'PENDING_PAYMENTS'")
    List<Tournament> findActiveTournaments();

    @Query("SELECT t FROM Tournament t WHERE t.status <> 'CANCELED' AND t.status <> 'FINISHED' AND t.status <> 'PENDING_PAYMENTS' AND t.sport.sportCategory = :sportCategory")
    List<Tournament> findActiveTournamentsBySportCategory(@Param("sportCategory") SportCategory sportCategory);

    @Query("SELECT t FROM Tournament t WHERE t.status <> 'CANCELED' AND t.status <> 'FINISHED' AND t.status <> 'PENDING_PAYMENTS' AND t.coordinator.id = :coordinatorId AND t.sport.sportCategory = :sportCategory")
    List<Tournament> findActiveTournamentsByCoordinator_CoordinatorIdAndSportCategory(@Param("coordinatorId") Integer coordinatorId, @Param("sportCategory") SportCategory sportCategory);

    List<Tournament> findByCoordinator_CoordinatorId(Integer coordinatorId);
}