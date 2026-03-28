package com.atl.backend.service.screen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.CityDTO;
import com.atl.backend.dto.CoordinatorDTO;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.City;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.AllTournamentsResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.FieldService;
import com.atl.backend.service.TournamentService;
import com.atl.backend.model.Field;

@Service
public class TournamentScreenService {
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private FieldService fieldService;



    @Transactional(readOnly = true)
    public AllTournamentsResponse getAllTournamentsScreen(SportCategory sportCategory) {
        List<Tournament> tournaments = tournamentService.getActiveTournamentsBySportCategory(sportCategory);
        Map<Integer, Coordinator> coordinatorCache = new HashMap<>();
        Map<Integer, Field> fieldsCache = new HashMap<>();
        BigDecimal prizeMin = BigDecimal.ZERO;
        BigDecimal prizeMax = BigDecimal.ZERO;
        BigDecimal feeMin = BigDecimal.ZERO;
        BigDecimal feeMax = BigDecimal.ZERO;

        List<TournamentDTO> myTournaments = AccountContext.isPlayer() || AccountContext.isCoordinator()
                ? tournamentService.getMyTournaments(sportCategory)
                : new ArrayList<>();

        tournaments.removeIf(
                t -> myTournaments.stream().anyMatch(fav -> fav.getId() == t.getId()));

        List<TournamentDTO> tournamentDTOs = new ArrayList<>();
        for (Tournament tournament : tournaments) {
            tournamentDTOs.add(TournamentDTO.fromTournament(tournament));
        }
        tournamentDTOs.addAll(myTournaments);
        tournamentDTOs.sort((a, b) -> a.getName().compareTo(b.getName()));

        for(TournamentDTO tournament : tournamentDTOs) {
            if (!coordinatorCache.containsKey(tournament.getCoordinatorId())) {
                Optional<Coordinator> optionalCoordinator = accountService.getCoordinatorByCoordinatorId(tournament.getCoordinatorId());
                if(optionalCoordinator.isEmpty()){
                    throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND); // This should never happen, but just in case
                }
                Coordinator coordinator = optionalCoordinator.get();
                coordinatorCache.put(tournament.getCoordinatorId(), coordinator);
            }
            if (!fieldsCache.containsKey(tournament.getFieldId())) {
                Optional<Field> optionalField = fieldService.getFieldById(tournament.getFieldId());
                if(optionalField.isEmpty()){
                    throw new NotFoundException(ErrorsString.FIELD_NOT_FOUND); // This should never happen, but just in case
                }
                Field field = optionalField.get();
                fieldsCache.put(tournament.getFieldId(), field);
                if (!fieldsCache.containsKey(field.getCity().getId())) {
                    String cityName = field.getCity().getName();
                    fieldsCache.put(field.getCity().getId(), field);
                }
            }
        }

        if (!tournamentDTOs.isEmpty()) {
            prizeMin = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getPrize1)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            prizeMax = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getPrize1)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            feeMin = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getFee)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            feeMax = tournamentDTOs
                    .stream()
                    .map(TournamentDTO::getFee)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        } else {
            prizeMin = BigDecimal.ZERO;
            prizeMax = BigDecimal.ZERO;
            feeMin = BigDecimal.ZERO;
            feeMax = BigDecimal.ZERO;
        }

        List<City> cities = fieldService.getAllCities();

        return AllTournamentsResponse.builder()
                .tournaments(tournamentDTOs)
                .myTournaments(myTournaments)
                .coordinatorsCache(coordinatorCache.entrySet().stream().collect(
                        HashMap::new,
                        (m, e) -> m.put(e.getKey(), CoordinatorDTO.fromCoordinator(e.getValue())),
                        HashMap::putAll))
                .fieldsCache(fieldsCache.entrySet().stream().collect(
                        HashMap::new,
                        (m, e) -> m.put(e.getKey(), FieldDTO.fromField(e.getValue())),
                        HashMap::putAll))
                .cities(cities.stream().map(CityDTO::fromCity).toList())
                .cityNamePerField(fieldsCache.values().stream()
                        .collect(HashMap::new, (m, f) -> m.put(f.getId(), f.getCity().getName()), HashMap::putAll))
                .prizeMin(prizeMin)
                .prizeMax(prizeMax)
                .feeMin(feeMin)
                .feeMax(feeMax)
                .build();
    }
}
