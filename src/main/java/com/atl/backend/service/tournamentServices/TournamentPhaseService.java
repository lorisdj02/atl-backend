package com.atl.backend.service.tournamentServices;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.TournamentPhase;
import com.atl.backend.model.enums.TournamentStatus;
import com.atl.backend.repository.FootballStandingRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.service.matchServices.MatchCombinationService;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.util.StandingsComparators;

@Service
public class TournamentPhaseService {
    @Autowired
    private TournamentAuthService tournamentAuthService;
    @Autowired
    private TournamentStandingService tournamentStandingService;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private MatchService matchService;
    @Autowired
    private MatchCombinationService matchCombinationService;

    @Autowired
    private FootballStandingRepository footballStandingRepository;

    private Tournament getExistingTournament(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Boolean canGoToNextPhase(Long tournamentId) {
        Tournament tournament = getExistingTournament(tournamentId);
        if (tournament.getStatus() != TournamentStatus.SCHEDULED) {
            throw new ConflictException(ErrorsString.TOURNAMENT_NOT_ACTIVE);
        }
        switch (tournament.getPhase()) {
            case GROUP:
                return matchService.areMatchesFinishedByTournamentIdAndGroupName(tournamentId, null);
            case RO32:
                return matchService.areMatchesFinishedByTournamentIdAndGroupName(tournamentId, "32");
            case RO16:
                return matchService.areMatchesFinishedByTournamentIdAndGroupName(tournamentId, "OF");
            case QUARTER:
                return matchService.areMatchesFinishedByTournamentIdAndGroupName(tournamentId, "QF");
            case SEMI:
                return matchService.areMatchesFinishedByTournamentIdAndGroupName(tournamentId, "SF");
            case FINAL:
                return matchService.areMatchesFinishedByTournamentIdAndGroupName(tournamentId, "FI");
            default:
                throw new IllegalArgumentException(ErrorsString.INVALID_TOURNAMENT_PHASE);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void goToCoordinatorNextPhase(Long tournamentId) {
        tournamentAuthService.checkManageTournament(tournamentId);
        if (!canGoToNextPhase(tournamentId)) {
            throw new ConflictException(ErrorsString.NOT_ALL_MATCHES_FINISHED);
        }
        goToNextPhase(tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void goToNextPhase(Long tournamentId) {
        tournamentAuthService.checkManageTournament(tournamentId);
        Tournament tournament = getExistingTournament(tournamentId);
        if (!canGoToNextPhase(tournamentId)) {
            throw new ConflictException(ErrorsString.NOT_ALL_MATCHES_FINISHED);
        }
        switch (tournament.getPhase()) {
            case GROUP:
                tournamentStandingService.updateTeamsQualifiedInTournament(tournament);
                tournament.setPhase(handlePostGroupStage(tournamentId));
                break;
            case RO32:
                // Non esiste ancora RO32, ma se dovesse essere aggiunta in futuro questa è la
                // logica da seguire
                tournament.setPhase(TournamentPhase.RO16);
                break;
            case RO16:
                tournament.setPhase(TournamentPhase.QUARTER);
                break;
            case QUARTER:
                tournament.setPhase(TournamentPhase.SEMI);
                break;
            case SEMI:
                tournament.setPhase(TournamentPhase.FINAL);
                break;
            case FINAL:
                throw new ConflictException(ErrorsString.WRONG_TOURNAMENT_PHASE);
        }
        tournament.setUpdatedAt(Instant.now());
        tournamentRepository.save(tournament);
    }

    @Transactional(rollbackFor = Exception.class)
    private TournamentPhase handlePostGroupStage(Long tournamentId) {
        tournamentAuthService.checkManageTournament(tournamentId);
        Tournament tournament = getExistingTournament(tournamentId);
        matchCombinationService.drawKnockoutStageMatches(tournamentId,
                getQualifiedTeamsPerRankByTournamentId(tournamentId));
        switch (tournament.getNumTeamsQualified()) {
            case 16:
                return TournamentPhase.RO16;
            case 8:
                return TournamentPhase.QUARTER;
            case 4:
                return TournamentPhase.SEMI;
            case 2:
                return TournamentPhase.FINAL;
            default:
                throw new IllegalArgumentException(ErrorsString.INVALID_NUMBER_OF_QUALIFIED_TEAMS);
        }
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<Team>> getQualifiedTeamsPerRankByTournamentId(Long tournamentId) {

        Tournament tournament = getExistingTournament(tournamentId);
        Map<Integer, List<Team>> qualifiedTeamsPerRank = new HashMap<>();
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                List<FootballStanding> footballStandings = footballStandingRepository.findByTournament_Id(tournamentId);
                Map<Integer, List<FootballStanding>> footballStandingsPerRank = new HashMap<>();
                for (FootballStanding footballStanding : footballStandings) {
                    if (!footballStanding.getQualified()) {
                        continue;
                    }
                    if (!footballStandingsPerRank.containsKey(Integer.valueOf(footballStanding.getRankPos()))) {
                        footballStandingsPerRank.put(Integer.valueOf(footballStanding.getRankPos()), new ArrayList<>());
                    }
                    footballStandingsPerRank.get(Integer.valueOf(footballStanding.getRankPos())).add(footballStanding);
                }

                for (Integer rank : footballStandingsPerRank.keySet()) {
                    List<FootballStanding> standingsForRank = footballStandingsPerRank.get(rank);
                    standingsForRank.sort(StandingsComparators.FOOTBALL_STANDING);
                    qualifiedTeamsPerRank.put(rank, standingsForRank.stream().map(FootballStanding::getTeam).toList());
                }

                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
        return qualifiedTeamsPerRank;
    }

}
