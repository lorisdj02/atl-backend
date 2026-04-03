package com.atl.backend.service.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.TeamDTO;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.Referee;
import com.atl.backend.model.Sport;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.response.AvailableMatchesResponse;
import com.atl.backend.response.PastAssignedMatchesResponse;
import com.atl.backend.service.MatchService;
import com.atl.backend.service.SportService;
import com.atl.backend.exception.*;

import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchScreenService {
    @Autowired
    private MatchService matchService;
    @Autowired
    private SportService sportService;

    @Transactional(readOnly = true)
    public AvailableMatchesResponse getAvailableMatchesScreen(SportCategory sportCategory) {
        Referee referee = AccountContext.getActiveReferee();
        List<Sport> sports = sportService.getSportsBySportCategory(sportCategory);
        Map<Integer, String> sportNames = new HashMap<>();
        Map<Integer, Team> teamsCache = new HashMap<>();
        Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();
        Map<Integer, Short> minutesPausePerTournament = new HashMap<>();

        switch (sportCategory) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = matchService.getScheduledFootballMatches();
                Map<Integer, List<FootballMatch>> matchesPerSport = new HashMap<>();
                for (Sport sport : sports) {
                    sportNames.put(sport.getId(), sport.getName());
                    matchesPerSport.put(sport.getId(), footballMatches.stream()
                            .filter(m -> m.getMatch().getTournament().getSport().getId().equals(sport.getId()))
                            .toList());
                }
                for (FootballMatch match : footballMatches) {
                    Tournament tournament = match.getMatch().getTournament();
                    Sport sport = tournament.getSport();
                    if (match.getHomeTeam() != null) {
                        teamsCache.put(match.getHomeTeam().getId(), match.getHomeTeam());
                    }
                    if (match.getAwayTeam() != null) {
                        teamsCache.put(match.getAwayTeam().getId(), match.getAwayTeam());
                    }
                    if (!minutesRoundPerTournament.containsKey(tournament.getId())) {
                        minutesRoundPerTournament.put(tournament.getId(),
                                sport.getMinutesPerRound());
                    }
                    if (!minutesPausePerTournament.containsKey(tournament.getId())) {
                        minutesPausePerTournament.put(tournament.getId(),
                                sport.getMinutesPerPause());
                    }
                }

                return AvailableMatchesResponse.builder()
                        .footballMatches(footballMatches.stream().map(FootballMatchDTO::fromFootballMatch).toList())
                        .teamsCache(teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> TeamDTO.fromTeam(e.getValue()))))
                        .minutesRoundPerTournament(minutesRoundPerTournament)
                        .minutesPausePerTournament(minutesPausePerTournament)
                        .footballMatchesPerSport(matchesPerSport.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        e -> e.getValue().stream().map(FootballMatchDTO::fromFootballMatch).toList())))
                        .sportNames(sportNames)
                        .build();
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    @Transactional(readOnly = true)
    public PastAssignedMatchesResponse getPastAssignedMatchesScreen(SportCategory sportCategory) {
        Referee referee = AccountContext.getActiveReferee();
        Map<Integer, Team> teamsCache = new HashMap<>();
        Map<Integer, Short> minutesRoundPerTournament = new HashMap<>();
        Map<Integer, Short> minutesPausePerTournament = new HashMap<>();
        switch (sportCategory) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = matchService
                        .getAssignedFootballMatchesByRefereeId(referee.getRefereeId());

                for (FootballMatch match : footballMatches) {
                    Sport sport = match.getMatch().getTournament().getSport();
                    Tournament tournament = match.getMatch().getTournament();
                    if (match.getHomeTeam() != null) {
                        teamsCache.put(match.getHomeTeam().getId(), match.getHomeTeam());
                    }
                    if (match.getAwayTeam() != null) {
                        teamsCache.put(match.getAwayTeam().getId(), match.getAwayTeam());
                    }
                    if (!minutesRoundPerTournament.containsKey(tournament.getId())) {
                        minutesRoundPerTournament.put(tournament.getId(),
                                sport.getMinutesPerRound());
                    }
                    if (!minutesPausePerTournament.containsKey(tournament.getId())) {
                        minutesPausePerTournament.put(tournament.getId(),
                                sport.getMinutesPerPause());
                    }
                }

                return PastAssignedMatchesResponse.builder()
                        .footballMatches(footballMatches.stream().map(FootballMatchDTO::fromFootballMatch).toList())
                        .teamsCache(teamsCache.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> TeamDTO.fromTeam(e.getValue()))))
                        .minutesRoundPerTournament(minutesRoundPerTournament)
                        .minutesPausePerTournament(minutesPausePerTournament)
                        .build();
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

    }
}
