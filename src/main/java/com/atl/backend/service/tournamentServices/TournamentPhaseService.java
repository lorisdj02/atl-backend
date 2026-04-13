package com.atl.backend.service.tournamentServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.TournamentPhase;
import com.atl.backend.model.enums.TournamentStatus;
import com.atl.backend.repository.FootballStandingRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.service.MatchService;
import com.atl.backend.util.StandingsComparators;

@Service
public class TournamentPhaseService {
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private MatchService matchService;
    @Autowired
    private FootballStandingRepository footballStandingRepository;

    private Tournament getExistingTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Boolean canGoToNextPhase(Integer tournamentId) {
        Tournament tournament = getExistingTournament(tournamentId);
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
    public void goToNextPhase(Integer tournamentId) {
        Tournament tournament = getExistingTournament(tournamentId);
        if (!canGoToNextPhase(tournamentId)) {
            throw new ConflictException(ErrorsString.NOT_ALL_MATCHES_FINISHED);
        }
        switch (tournament.getPhase()) {
            case GROUP:
                updateTeamsQualifiedInTournament(tournamentId, true);
                tournament.setPhase(handlePostGroupStage(tournamentId));
                break;
            case RO32:
                // Non esiste ancora RO32, ma se dovesse essere aggiunta in futuro questa è la
                // logica da seguire
                tournament.setPhase(TournamentPhase.RO16);
                break;
            case RO16:
                if (!canGoToNextPhase(tournamentId)) {
                    throw new ConflictException(ErrorsString.NOT_ALL_MATCHES_FINISHED);
                }
                tournament.setPhase(TournamentPhase.QUARTER);
                break;
            case QUARTER:
                tournament.setPhase(TournamentPhase.SEMI);
                break;
            case SEMI:
                tournament.setPhase(TournamentPhase.FINAL);
                break;
            case FINAL:
                giveTournamentPrizes(tournamentId);
                break;
        }
        tournamentRepository.save(tournament);
    }

    @Transactional(rollbackFor = Exception.class)
    private TournamentPhase handlePostGroupStage(Integer tournamentId) {
        Tournament tournament = getExistingTournament(tournamentId);
        matchService.drawKnockoutStageMatches(tournamentId, getQualifiedTeamsPerRankByTournamentId(tournamentId));
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
    public Map<Integer, List<Team>> getQualifiedTeamsPerRankByTournamentId(Integer tournamentId) {

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
                        footballStandingsPerRank.put((int) footballStanding.getRankPos(), new ArrayList<>());
                    }
                    footballStandingsPerRank.get((int) footballStanding.getRankPos()).add(footballStanding);
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

    @Transactional(rollbackFor = Exception.class)
    public void updateTeamsQualifiedInTournament(Integer tournamentId, Boolean needToOrder) {
        Tournament tournament = getExistingTournament(tournamentId);

        final short qualifiedPerGroup = (short) (tournament.getNumTeamsQualified() / tournament.getNumGroups());
        final short numTeamsPerGroup = (short) (tournament.getNumTeams() / tournament.getNumGroups());
        int extraQualified = 0;

        if ((tournament.getNumTeamsQualified() % tournament.getNumGroups()) != 0) {
            extraQualified = tournament.getNumTeamsQualified() % tournament.getNumGroups();
        }

        if (needToOrder) {
            orderStandingsForTournament(tournament.getId());
        }

        System.out.println("[DEBUG - TournamentService] - Updating qualified teams for tournament " + tournamentId
                + " with " + qualifiedPerGroup + " qualified per group and " + extraQualified + " extra qualified");

        List<FootballStanding> notQualifiedFootballStandings = new ArrayList<>();

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                for (int groupIndex = 1; groupIndex <= tournament.getNumGroups(); groupIndex++) {
                    String groupName = "" + (char) ('A' + groupIndex - 1);
                    System.out.println(
                            "[DEBUG - TournamentService] - Evaluating qualification for tournament " + tournamentId
                                    + " and group " + groupName);

                    List<FootballStanding> footballStandings = footballStandingRepository
                            .findByTournament_IdAndGroupName(tournamentId, groupName);

                    System.out.println("[DEBUG - TournamentService] - Football standings for tournament " + tournamentId
                            + " and group " + groupName + ". " + footballStandings.size() + " teams");
                    for (int i = 0; i < footballStandings.size(); i++) {
                        if (i < qualifiedPerGroup) {
                            FootballStanding footballStanding = footballStandings.get(i);
                            footballStanding.setQualified(true);
                            footballStandingRepository.save(footballStanding);
                        } else {
                            FootballStanding footballStanding = footballStandings.get(i);
                            footballStanding.setQualified(false);
                            footballStandingRepository.save(footballStanding);
                            notQualifiedFootballStandings.add(footballStanding);
                        }
                    }
                }

                for (short consideredPos = 1; consideredPos <= numTeamsPerGroup
                        && extraQualified > 0; consideredPos++) {
                    int takenExtraQualified = 0;
                    List<FootballStanding> notQualifiedForPos = new ArrayList<>();

                    for (FootballStanding footballStanding : notQualifiedFootballStandings) {
                        if (footballStanding.getRankPos() == consideredPos) {
                            notQualifiedForPos.add(footballStanding);
                        }
                    }
                    if (notQualifiedForPos.size() > 0 && extraQualified > 0) {
                        notQualifiedForPos.sort(StandingsComparators.FOOTBALL_STANDING);
                        for (int i = 0; i < Math.min(extraQualified, notQualifiedForPos.size()); i++) {
                            FootballStanding footballStanding = notQualifiedForPos.get(i);
                            footballStanding.setQualified(true);
                            footballStandingRepository.save(footballStanding);
                            takenExtraQualified++;
                        }
                        extraQualified -= takenExtraQualified;
                    }
                }

                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public void orderStandingsForTournament(Integer tournamentId) {
        Tournament tournament = getExistingTournament(tournamentId);
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                List<FootballStanding> footballStandings = new ArrayList<>(
                        footballStandingRepository.findByTournament_Id(tournamentId));
                System.out.println("[DEBUG - TournamentService] - Ordering standings for tournament " + tournamentId
                        + ". " + footballStandings.size() + " teams");
                List<FootballStanding> groupFootballStandings = new ArrayList<>();
                for (int j = 0; j < tournament.getNumGroups(); j++) {
                    groupFootballStandings = new ArrayList<>();
                    String groupName = "" + (char) ('A' + j);
                    for (FootballStanding footballStanding : footballStandings) {
                        if (footballStanding.getGroupName().equals(groupName)) {
                            groupFootballStandings.add(footballStanding);
                        }
                    }
                    groupFootballStandings.sort(StandingsComparators.FOOTBALL_STANDING);
                    System.out.println("[DEBUG - TournamentService] - Ordering standings for group " + groupName + ". "
                            + groupFootballStandings.size() + " teams");
                    for (int i = 0; i < groupFootballStandings.size(); i++) {
                        FootballStanding footballStanding = groupFootballStandings.get(i);
                        footballStanding.setRankPos((short) (i + 1));
                        System.out.println(
                                "[DEBUG - TournamentService] - Setting rank position " + footballStanding.getRankPos()
                                        + " for team " + footballStanding.getTeam().getName() + " "
                                        + footballStanding.getTeam().getId() + " in tournament "
                                        + tournamentId + " and group " + groupName);
                        footballStandingRepository.save(footballStanding);
                    }
                }

                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private void giveTournamentPrizes(Integer tournamentId) {

        Tournament tournament = getExistingTournament(tournamentId);

        if (!AccountContext.getCoordinatorId().equals(tournament.getCoordinator().getCoordinatorId())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_TOURNAMENT_COORDINATOR);
        }

        tournament.setStatus(TournamentStatus.PENDING_PAYMENTS);
        tournamentRepository.save(tournament);

        // TODO: Devi continuare la logica di assegnazione premi
    }
}
