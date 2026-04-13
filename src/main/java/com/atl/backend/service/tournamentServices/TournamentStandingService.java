package com.atl.backend.service.tournamentServices;

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
import com.atl.backend.service.MatchService;
import com.atl.backend.service.TeamService;

@Service
public class TournamentStandingService {

    @Autowired
    private FootballStandingRepository footballStandingRepository;
    @Autowired
    private MatchService matchService;
    @Autowired
    private TeamService teamService;


    public List<FootballStanding> getFootballStandingsByTournamentId(Integer tournamentId) {
        return footballStandingRepository.findByTournament_Id(tournamentId);
    }

    public void deleteAllFootballStandings(){
        footballStandingRepository.deleteAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public FootballStanding updateFootballStanding(Integer tournamentId, Integer teamId, Short ptsDelta,
            Short goalsConcededDelta, Short goalsScoredDelta, Short wonDelta, Short drawnDelta, Short lostDelta) {

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

    public void generateStandingsForTournament(Tournament tournament) {
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                for (int groupIndex = 1; groupIndex <= tournament.getNumGroups(); groupIndex++) {
                    String groupName = "" + (char) ('A' + groupIndex - 1);
                    List<Integer> teamIds = matchService.getTeamIdsByTournamentIdAndGroupStage(tournament.getId(),
                            groupName);

                    System.out.println(
                            "[DEBUG - TournamentService] - Generating standings for tournament " + tournament.getId()
                                    + " and group " + groupName + " with teams " + teamIds);
                    for (Integer teamId : teamIds) {
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
}
