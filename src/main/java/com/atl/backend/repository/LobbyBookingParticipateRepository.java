package com.atl.backend.repository;

import com.atl.backend.model.LobbyBookingParticipate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LobbyBookingParticipateRepository extends JpaRepository<LobbyBookingParticipate, Integer> {
}