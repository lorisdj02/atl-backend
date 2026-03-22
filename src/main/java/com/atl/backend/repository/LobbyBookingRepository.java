package com.atl.backend.repository;

import com.atl.backend.model.LobbyBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface LobbyBookingRepository extends JpaRepository<LobbyBooking, Integer> {

    List<LobbyBooking> findByOwnerPlayer_PlayerId(Integer ownerPlayerId);

    List<LobbyBooking> findByBooking_Field_Id(Integer fieldId);
}