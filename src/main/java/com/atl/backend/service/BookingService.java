package com.atl.backend.service;

import org.springframework.stereotype.Service;

import com.atl.backend.repository.BookingRepository;
import com.atl.backend.repository.LobbyBookingRepository;
import com.atl.backend.repository.PrivateBookingRepository;
import com.atl.backend.repository.LobbyBookingParticipateRepository;

import com.atl.backend.model.PrivateBooking;
import com.atl.backend.model.enums.BookingStatus;
import com.atl.backend.model.enums.TransactionStatus;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.model.LobbyBookingParticipate;

import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final LobbyBookingRepository lobbyBookingRepository;
    private final PrivateBookingRepository privateBookingRepository;
    private final LobbyBookingParticipateRepository lobbyBookingParticipateRepository;

    public BookingService(BookingRepository bookingRepository, LobbyBookingRepository lobbyBookingRepository,
            PrivateBookingRepository privateBookingRepository,
            LobbyBookingParticipateRepository lobbyBookingParticipateRepository) {
        this.bookingRepository = bookingRepository;
        this.lobbyBookingRepository = lobbyBookingRepository;
        this.privateBookingRepository = privateBookingRepository;
        this.lobbyBookingParticipateRepository = lobbyBookingParticipateRepository;
    }

    public List<PrivateBooking> getPrivateBookingsByPlayerId(Integer playerId) {
        return privateBookingRepository.findByPlayer_PlayerId(playerId);
    }

    public List<PrivateBooking> getActivePrivateBookingsByPlayerId(Integer playerId) {
        List<PrivateBooking> privateBookings = privateBookingRepository.findByPlayer_PlayerId(playerId);
        List<PrivateBooking> activePrivateBookings = List.of();
        for (PrivateBooking booking : privateBookings) {
            if (booking.getBooking().getStatus() != BookingStatus.CANCELED
                    && booking.getBooking().getStatus() != BookingStatus.FINISHED) {
                activePrivateBookings.add(booking);
            }
        }
        return activePrivateBookings;
    }

    public List<LobbyBooking> getLobbyBookingsByOwnerPlayerId(Integer ownerPlayerId) {
        return lobbyBookingRepository.findByOwnerPlayer_PlayerId(ownerPlayerId);
    }

    public List<LobbyBooking> getLobbyBookingsByPlayerId(Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository.findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = List.of();
        for (LobbyBookingParticipate participation : participations) {
            lobbyBookings.add(participation.getLobbyBooking());
        }
        return lobbyBookings;
    }

    public List<LobbyBooking> getActiveLobbyBookingsByPlayerId(Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository.findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = List.of();
        for (LobbyBookingParticipate participation : participations) {
            if (participation.getLobbyBooking().getBooking().getStatus() != BookingStatus.CANCELED
                    && participation.getLobbyBooking().getBooking().getStatus() != BookingStatus.FINISHED) {
                lobbyBookings.add(participation.getLobbyBooking());
            }
        }
        return lobbyBookings;
    }

    public List<LobbyBooking> getNotRefundedLobbyBookingsByPlayerId(Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository.findByPlayer_PlayerId(playerId);
        List<LobbyBooking> lobbyBookings = List.of();
        for (LobbyBookingParticipate participation : participations) {
            if (participation.getTransactionGroup().getStatus() != TransactionStatus.REFUNDED) {
                lobbyBookings.add(participation.getLobbyBooking());
            }
        }
        return lobbyBookings;
    }

    public Short getPlayersPaidInLobbyBookingPerPlayerId(Integer bookingId, Integer playerId) {
        List<LobbyBookingParticipate> participations = lobbyBookingParticipateRepository
                .findByLobbyBooking_IdAndPlayer_PlayerId(bookingId,
                        playerId)
                .stream().filter(p -> p.getTransactionGroup().getStatus() != TransactionStatus.CONFIRMED)
                .toList();
        
        if(participations.size() > 0) {
            return 0;
        }
        return participations.get(0).getPlayersPaid();
    }
}
