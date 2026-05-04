package com.atl.backend.service.bookingServices;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.MatchBookingDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Booking;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.Match;
import com.atl.backend.model.MatchBooking;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.BookingType;
import com.atl.backend.model.enums.FootballMatchStatus;
import com.atl.backend.model.enums.TournamentPhase;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.repository.MatchBookingRepository;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.service.tournamentServices.TournamentAuthService;

@Service
public class MatchBookingService {

    @Autowired
    private TournamentAuthService tournamentAuthService;
    @Autowired
    private BookingUtilitiesService bookingUtilitiesService;
    @Autowired
    private MatchBookingRepository matchBookingRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private FootballMatchRepository footballMatchRepository;

    @Transactional(rollbackFor = Exception.class)
    public MatchBooking createMatchBooking(Long matchId, Instant startTime,
            Instant finishTime) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));

        Booking booking = bookingUtilitiesService.createBooking(match.getTournament().getField().getId(),
                startTime.toString(),
                finishTime.toString(), match.getTournament().getSport().getSportCategory().toString(),
                BookingType.MATCH);

        MatchBooking matchBooking = new MatchBooking();
        matchBooking.setBooking(booking);
        matchBooking.setMatch(match);
        matchBooking.setCoordinator(match.getTournament().getCoordinator());
        return matchBookingRepository.save(matchBooking);
    }

    @Transactional(rollbackFor = Exception.class)
    public MatchBookingDTO scheduleMatch(Long matchId, Instant scheduledStart) {
        // Il match deve essere schedulato almeno 2 ore prima dell'orario attuale
        if (scheduledStart.isBefore(Instant.now().plusSeconds(2 * 3600))) {
            throw new BadRequestException(ErrorsString.MATCH_SCHEDULE_TOO_LATE);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));

        Tournament tournament = match.getTournament();

        String group = match.getGroupName();
        if (group.length() > 1) {
            if (group.startsWith("32") && tournament.getPhase() != TournamentPhase.RO32) {
                throw new ConflictException(ErrorsString.CANNOT_SCHEDULE_MATCH);
            } else if (group.startsWith("OF") && tournament.getPhase() != TournamentPhase.RO16) {
                throw new ConflictException(ErrorsString.CANNOT_SCHEDULE_MATCH);
            } else if (group.startsWith("QF") && tournament.getPhase() != TournamentPhase.QUARTER) {
                throw new ConflictException(ErrorsString.CANNOT_SCHEDULE_MATCH);
            } else if (group.startsWith("SF") && tournament.getPhase() != TournamentPhase.SEMI) {
                throw new ConflictException(ErrorsString.CANNOT_SCHEDULE_MATCH);
            } else if (group.startsWith("FI") && tournament.getPhase() != TournamentPhase.FINAL) {
                throw new ConflictException(ErrorsString.CANNOT_SCHEDULE_MATCH);
            }
        }

        if (!tournament.isTournamentActive()) {
            throw new ConflictException(ErrorsString.TOURNAMENT_NOT_ACTIVE);
        }

        tournamentAuthService.canManageTournament(tournament.getId());

        Short minutesPerRound = tournament.getSport().getMinutesPerRound();
        Short minutesPerPause = tournament.getSport().getMinutesPerPause();

        // Considero 15 minuti di preparazione
        Instant startTime = scheduledStart.minusSeconds(15 * 60);

        // Considero la durata massima della partita, che è 2 round più una pausa, più
        // 15 minuti di buffer finale
        Instant finishTime = scheduledStart.plusSeconds((minutesPerRound * 2 + minutesPerPause + 15) * 60);

        if (!bookingUtilitiesService.isFieldFree(tournament.getField().getId(), startTime, finishTime)) {
            throw new ConflictException(ErrorsString.FIELD_NOT_AVAILABLE);
        }
        if (scheduledStart.equals(match.getScheduledStart())) {
            throw new ConflictException(ErrorsString.MATCH_ALREADY_SCHEDULED_FOR_THIS_TIME);
        } else if (match.getScheduledStart() != null
                && Instant.now().plusSeconds(24 * 3600).isAfter(match.getScheduledStart())) {
            throw new ConflictException(ErrorsString.YOU_CANNOT_RESCHEDULE_MATCH_LESS_THAN_24_HOURS_BEFORE);
        }
        MatchBooking matchBooking;
        if (match.getScheduledStart() == null) {
            matchBooking = createMatchBooking(matchId, startTime, finishTime);
        } else {
            Optional<MatchBooking> existingMatchBooking = matchBookingRepository.findByMatch_Id(matchId);
            if (existingMatchBooking.isPresent()) {
                matchBooking = existingMatchBooking.get();
                matchBooking.getBooking().setStartTime(startTime);
                matchBooking.getBooking().setFinishTime(finishTime);
                matchBooking.getBooking().setUpdatedAt(Instant.now());
                matchBookingRepository.save(matchBooking);
            } else {
                matchBooking = createMatchBooking(matchId, startTime, finishTime);
            }
        }

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                FootballMatch footballMatch = footballMatchRepository.findByMatch_Id(matchId)
                        .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
                footballMatch.setStatus(FootballMatchStatus.SCHEDULED);
                footballMatch.setUpdatedAt(Instant.now());
                footballMatchRepository.save(footballMatch);
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

        match.setScheduledStart(scheduledStart);
        match.setUpdatedAt(Instant.now());
        matchRepository.save(match);

        return MatchBookingDTO.fromMatchBooking(matchBooking);
    }

    public List<MatchBooking> getMatchBookingsByFieldId(Long fieldId) {
        return matchBookingRepository.findByBooking_Field_Id(fieldId);
    }

    public Optional<MatchBooking> getMatchBookingByBookingId(Long bookingId) {
        return matchBookingRepository.findById(bookingId);
    }

}
