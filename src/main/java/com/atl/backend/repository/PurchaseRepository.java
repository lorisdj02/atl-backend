package com.atl.backend.repository;

import com.atl.backend.model.Purchase;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {

    @Query("SELECT p FROM Purchase p WHERE p.type = 'TOURNAMENT_FEE' AND p.objectId = :tournamentId AND p.account.id = :accountId")
    List<Purchase> findByTournament_IdAndAccount_Id(Integer tournamentId, Integer accountId);
}