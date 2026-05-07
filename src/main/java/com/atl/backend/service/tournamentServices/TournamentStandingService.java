package com.atl.backend.service.tournamentServices;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.Tournament;
import com.atl.backend.repository.FootballStandingRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.service.matchServices.MatchService;
import com.atl.backend.service.teamServices.TeamService;
import com.atl.backend.util.StandingsComparators;

@Service
public class TournamentStandingService {

    @Autowired
    private TournamentAuthService tournamentAuthService;
    @Autowired
    private FootballStandingRepository footballStandingRepository;
    @Autowired
    private MatchService matchService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TournamentRepository tournamentRepository;

    public List<FootballStanding> getFootballStandingsByTournamentId(Long tournamentId) {
        return footballStandingRepository.findByTournament_Id(tournamentId);
    }

    public void deleteAllFootballStandings() {
        footballStandingRepository.deleteAll();
    }

    private Tournament getExistingTournament(Long tournamentId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        return tournamentOptional.get();
    }

    @Transactional(rollbackFor = Exception.class)
    public FootballStanding updateFootballStanding(Long tournamentId, Long teamId, Short ptsDelta,
            Short goalsScoredDelta, Short goalsConcededDelta, Short wonDelta, Short drawnDelta, Short lostDelta) {

        Optional<FootballStanding> footballStandingOptional = footballStandingRepository
                .findByTournament_IdAndTeam_Id(tournamentId, teamId);

        if (footballStandingOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.STANDING_NOT_FOUND);
        }

        ptsDelta = ptsDelta != null ? ptsDelta : 0;
        goalsConcededDelta = goalsConcededDelta != null ? goalsConcededDelta : 0;
        goalsScoredDelta = goalsScoredDelta != null ? goalsScoredDelta : 0;
        wonDelta = wonDelta != null ? wonDelta : 0;
        drawnDelta = drawnDelta != null ? drawnDelta : 0;
        lostDelta = lostDelta != null ? lostDelta : 0;

        FootballStanding footballStanding = footballStandingOptional.get();

        footballStanding.setPts((short) (footballStanding.getPts() + ptsDelta));
        footballStanding.setGoalsConceded((short) (footballStanding.getGoalsConceded() + goalsConcededDelta));
        footballStanding.setGoalsScored((short) (footballStanding.getGoalsScored() + goalsScoredDelta));
        footballStanding.setWon((short) (footballStanding.getWon() + wonDelta));
        footballStanding.setDrawn((short) (footballStanding.getDrawn() + drawnDelta));
        footballStanding.setLost((short) (footballStanding.getLost() + lostDelta));

        return footballStandingRepository.save(footballStanding);
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateStandingsForTournament(Tournament tournament) {

        tournamentAuthService.checkManageTournament(tournament.getId());

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                for (int groupIndex = 1; groupIndex <= tournament.getNumGroups(); groupIndex++) {
                    String groupName = "" + (char) ('A' + groupIndex - 1);
                    List<Long> teamIds = matchService.getTeamIdsByTournamentIdAndGroupStage(tournament.getId(),
                            groupName);

                    System.out.println(
                            "[DEBUG - TournamentService] - Generating standings for tournament " + tournament.getId()
                                    + " and group " + groupName + " with teams " + teamIds);
                    for (Long teamId : teamIds) {
                        FootballStanding footballStanding = new FootballStanding();
                        footballStanding.setTournament(tournament);
                        footballStanding.setTeam(teamService.getTeamById(teamId).get());
                        footballStanding.setGroupName(groupName);
                        footballStandingRepository.save(footballStanding);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTeamsQualifiedInTournament(Tournament tournament) {

        final short qualifiedPerGroup = (short) (tournament.getNumTeamsQualified() / tournament.getNumGroups());
        final short numTeamsPerGroup = (short) (tournament.getNumTeams() / tournament.getNumGroups());
        int extraQualified = 0;

        if ((tournament.getNumTeamsQualified() % tournament.getNumGroups()) != 0) {
            extraQualified = tournament.getNumTeamsQualified() % tournament.getNumGroups();
        }

        orderStandingsForTournament(tournament.getId());

        List<FootballStanding> notQualifiedFootballStandings = new ArrayList<>();

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                for (int groupIndex = 1; groupIndex <= tournament.getNumGroups(); groupIndex++) {
                    String groupName = "" + (char) ('A' + groupIndex - 1);

                    List<FootballStanding> footballStandings = footballStandingRepository
                            .findByTournament_IdAndGroupName(tournament.getId(), groupName);
                    footballStandings.sort((a, b) -> a.getRankPos().equals(b.getRankPos()) ? 0
                            : a.getRankPos() > b.getRankPos() ? 1 : -1); // ordino in base alla posizione in classifica
                    for (int i = 0; i < footballStandings.size(); i++) {
                        FootballStanding footballStanding = footballStandings.get(i);
                        footballStanding.setQualified(false);
                        if (i < qualifiedPerGroup) {
                            footballStanding.setQualified(true);
                        } else {
                            notQualifiedFootballStandings.add(footballStanding);
                        }
                        footballStandingRepository.save(footballStanding);
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

    @Transactional(rollbackFor = Exception.class)
    public void orderStandingsForTournament(Long tournamentId) {
        Tournament tournament = getExistingTournament(tournamentId);
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                List<FootballStanding> footballStandings = new ArrayList<>(
                        footballStandingRepository.findByTournament_Id(tournamentId));
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

                    for (int i = 0; i < groupFootballStandings.size(); i++) {
                        FootballStanding footballStanding = groupFootballStandings.get(i);
                        footballStanding.setRankPos((short) (i + 1));
                        footballStanding.setUpdatedAt(Instant.now());
                        footballStandingRepository.save(footballStanding);
                    }
                }

                break;
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    
}
