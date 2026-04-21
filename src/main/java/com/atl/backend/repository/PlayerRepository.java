package com.atl.backend.repository;

import com.atl.backend.model.Player;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByAccount_Id(Long accountId);

    Optional<Player> findByPlayerId(Long playerId);

    void deleteByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);

    boolean existsByPlayerId(Long playerId);

}