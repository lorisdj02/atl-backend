package com.atl.backend.repository;

import com.atl.backend.model.LobbyBookingParticipate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LobbyBookingParticipateRepository extends JpaRepository<LobbyBookingParticipate, Long> {
    List<LobbyBookingParticipate> findByLobbyBooking_IdAndPlayer_PlayerId(Long lobbyBookingId, Long playerId);

    List<LobbyBookingParticipate> findByLobbyBooking_Id(Long bookingId);

    List<LobbyBookingParticipate> findByPlayer_PlayerId(Long playerId);

    @Query("SELECT lbp FROM LobbyBookingParticipate lbp WHERE lbp.player.playerId = :playerId AND lbp.lobbyBooking.id = :bookingId")
    Optional<LobbyBookingParticipate> findByPlayer_PlayerIdAndLobbyBooking_Booking_Id(Long playerId, Long bookingId);
}