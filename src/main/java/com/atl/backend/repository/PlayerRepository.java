package com.atl.backend.repository;

import com.atl.backend.model.Player;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {

    Optional<Player> findByAccount_Id(Integer accountId);

    Optional<Player> findByPlayerId(Integer playerId);

    void deleteByAccount_Id(Integer accountId);
}