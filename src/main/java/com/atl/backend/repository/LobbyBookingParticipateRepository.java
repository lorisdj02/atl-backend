package com.atl.backend.repository;

import com.atl.backend.model.LobbyBookingParticipate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LobbyBookingParticipateRepository extends JpaRepository<LobbyBookingParticipate, Integer> {
    List<LobbyBookingParticipate> findByLobbyBooking_IdAndPlayer_PlayerId(Integer lobbyBookingId, Integer playerId);

    List<LobbyBookingParticipate> findByLobbyBooking_Id(Integer bookingId);

    List<LobbyBookingParticipate> findByPlayer_PlayerId(Integer playerId);
}