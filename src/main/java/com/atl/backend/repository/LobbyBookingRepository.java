package com.atl.backend.repository;

import com.atl.backend.model.LobbyBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LobbyBookingRepository extends JpaRepository<LobbyBooking, Integer> {
}