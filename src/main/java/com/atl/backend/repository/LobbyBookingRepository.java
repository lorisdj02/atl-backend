package com.atl.backend.repository;

import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.enums.SportCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LobbyBookingRepository extends JpaRepository<LobbyBooking, Long> {

    List<LobbyBooking> findByOwnerPlayer_PlayerId(Long ownerPlayerId);

    List<LobbyBooking> findByBooking_Field_Id(Long fieldId);

    @Query("SELECT lb FROM LobbyBooking lb WHERE lb.booking.status = 'BOOKED' AND lb.booking.sportCategory = :sportCategory")
    List<LobbyBooking> findBookedByBooking_SportCategory(
            SportCategory sportCategory);
}