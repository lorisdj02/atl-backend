package com.atl.backend.service.screen;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CityDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.LobbyBookingDTO;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.City;
import com.atl.backend.model.Player;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.model.LobbyBooking;
import com.atl.backend.response.JoinLobbyResponse;
import com.atl.backend.response.LobbyInfoResponse;
import com.atl.backend.response.BookFieldResponse;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.accountServices.PlayerService;
import com.atl.backend.service.bookingServices.LobbyBookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingScreenService {
        @Autowired
        private LobbyBookingService lobbyBookingService;
        @Autowired
        private FieldService fieldService;
        @Autowired
        private PlayerService playerService;

        @Transactional(readOnly = true)
        public BookFieldResponse getBookFieldScreen(SportCategory sportCategory) {
                Map<Long, LocalTime> minOpeningTimePerCity = new HashMap<>();
                Map<Long, LocalTime> maxClosingTimePerCity = new HashMap<>();
                List<City> cities = fieldService.getAllCities();
                for (City city : cities) {
                        minOpeningTimePerCity.put(city.getId(),
                                        fieldService.getMinOpeningTimeByCityId(city.getId(), sportCategory));
                        maxClosingTimePerCity.put(city.getId(),
                                        fieldService.getMaxClosingTimeByCityId(city.getId(), sportCategory));
                }
                return BookFieldResponse.builder()
                                .cities(cities.stream().map(CityDTO::fromCity).toList())
                                .minOpeningTimePerCity(minOpeningTimePerCity)
                                .maxClosingTimePerCity(maxClosingTimePerCity)
                                .build();
        }

        @Transactional(readOnly = true)
        public JoinLobbyResponse getJoinLobbyScreen(SportCategory sportCategory, Short totalPlayers,
                        Long selectedCityId, LocalDateTime startTime, LocalDateTime finishTime) {

                Long playerId = AccountContext.getPlayerId();
                Optional<Player> optionalPlayer = playerService.getPlayerByPlayerId(playerId);
                if (optionalPlayer.isEmpty()) {
                        throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
                }

                Map<Long, String> usernamePerBooking = new HashMap<>();
                Map<Long, Short> maxPlayersPerLobby = new HashMap<>();
                Map<Long, Short> currentPlayersPerLobby = new HashMap<>();
                Map<Long, Short> playersPaidPerBooking = new HashMap<>();
                Map<Long, FieldDTO> fieldsCache = new HashMap<>();
                Map<Long, String> completeAddressPerField = new HashMap<>();
                List<CityDTO> cities = fieldService.getAllCities().stream().map(CityDTO::fromCity).toList();

                List<LobbyBooking> lobbies = lobbyBookingService.getBookedLobbyBookings(sportCategory, totalPlayers,
                                selectedCityId,
                                startTime, finishTime);
                for (LobbyBooking lobbyBooking : lobbies) {
                        LobbyInfoResponse lobbyInfoResponse = lobbyBookingService.getLobbyInfo(
                                        lobbyBooking.getBooking().getId(),
                                        playerId);
                        usernamePerBooking.put(lobbyBooking.getBooking().getId(),
                                        lobbyInfoResponse.getUsernamePerBooking());
                        maxPlayersPerLobby.put(lobbyBooking.getBooking().getId(),
                                        lobbyInfoResponse.getMaxPlayersPerLobby());
                        currentPlayersPerLobby.put(lobbyBooking.getBooking().getId(),
                                        lobbyInfoResponse.getCurrentPlayersPerLobby());
                        playersPaidPerBooking.put(lobbyBooking.getBooking().getId(),
                                        lobbyInfoResponse.getPlayersPaidPerBooking());
                        fieldsCache.put(lobbyBooking.getBooking().getId(), lobbyInfoResponse.getField());
                        completeAddressPerField.put(lobbyBooking.getBooking().getId(),
                                        lobbyInfoResponse.getCompleteAddressPerField());
                }
                return JoinLobbyResponse.builder()
                                .lobbies(lobbies.stream().map(LobbyBookingDTO::fromLobbyBooking).toList())
                                .usernamePerBooking(usernamePerBooking)
                                .maxPlayersPerLobby(maxPlayersPerLobby)
                                .currentPlayersPerLobby(currentPlayersPerLobby)
                                .playersPaidPerBooking(playersPaidPerBooking)
                                .fieldsCache(fieldsCache)
                                .completeAddressPerField(completeAddressPerField)
                                .cities(cities)
                                .build();
        }
}
