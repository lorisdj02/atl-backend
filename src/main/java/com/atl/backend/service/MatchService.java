package com.atl.backend.service;

import com.atl.backend.repository.FinalStageBracketRepository;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.repository.RefereeAssignmentRepository;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.atl.backend.model.enums.*;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.*;
import com.atl.backend.model.FinalStageBracket;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.Match;
import com.atl.backend.model.RefereeAssignment;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.LegOrderType;
import com.atl.backend.model.enums.MatchSlotType;
import com.atl.backend.model.enums.SportCategory;

@Service
public class MatchService {
    @Autowired
    private TeamService teamService;
    @Autowired
    private FootballMatchRepository footballMatchRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private FinalStageBracketRepository finalStageBracketRepository;
    @Autowired
    private RefereeAssignmentRepository refereeAssignmentRepository;

    public Integer calculateMatchesInTournament(Integer tournamentId) {
        Optional<Tournament> tournamentOptional = tournamentService.getTournamentById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }

        final Tournament tournament = tournamentOptional.get();
        final int numTeams = tournament.getNumTeams();
        final int numGroups = tournament.getNumGroups();
        final int numTeamsQualified = tournament.getNumTeamsQualified();

        final int numTeamsPerGroup = numTeams / numGroups;
        final int round2GroupStage = tournament.getRound2GroupStage() ? 1 : 0;
        final int round2FinalStage = tournament.getRound2FinalStage() ? 1 : 0;

        final int matchesInGroupStage = numGroups * (numTeamsPerGroup * (numTeamsPerGroup - 1))
                / (2 - round2GroupStage);
        final int matchesInFinalStage = numTeamsQualified - 1 + round2FinalStage * (numTeamsQualified - 2);
        return matchesInGroupStage + matchesInFinalStage;
    }

    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    public List<FootballMatch> getAllFootballMatches() {
        return footballMatchRepository.findAll();
    }

    public Optional<FootballMatch> getFootballMatchById(Integer id) {
        return footballMatchRepository.findById(id);
    }

    public FootballMatch saveFootballMatch(FootballMatch match) {
        return footballMatchRepository.save(match);
    }

    public void deleteFootballMatch(Integer id) {
        footballMatchRepository.deleteById(id);
    }

    public List<FootballMatch> getFootballMatchesByTournamentId(Integer tournamentId) {
        return footballMatchRepository.findByMatch_Tournament_Id(tournamentId);
    }

    private record KnockoutPhase(int qualified, String prefix, int matches) {
    }

    private record BracketStage(String nextGroup, String homeGroup, String awayGroup) {
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateMatchesForTournament(Integer tournamentId) {
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }

        System.out.println("[DEBUG - MatchService] - Generating matches for tournament " + tournamentId);

        final Tournament tournament = optionalTournament.get();

        List<Integer> teamIds = new ArrayList<>(tournamentService.getTeamIdsParticipatingInTournament(tournamentId));

        System.out
                .println("[DEBUG - MatchService] - Retrieved team IDs for tournament " + tournamentId + ": " + teamIds);

        Collections.shuffle(teamIds);

        System.out
                .println("[DEBUG - MatchService] - Teams participating in tournament " + tournamentId + ": " + teamIds);

        final SportCategory sportCategory = tournament.getSport().getSportCategory();

        System.out.println("[DEBUG - MatchService] - Generating matches for group stage of tournament " + tournamentId);

        generateMatchesForGroupStage(tournament, teamIds, sportCategory);

        System.out.println("[DEBUG - MatchService] - Generating matches for final stage of tournament " + tournamentId);

        generateMatchesForFinalStage(tournament, sportCategory);

    }

    private void generateMatchesForGroupStage(Tournament tournament, List<Integer> teamIds,
            SportCategory sportCategory) {
        final int numTeamsPerGroup = tournament.getNumTeams() / tournament.getNumGroups();

        System.out.println("[DEBUG - MatchService] - Number of teams per group: " + numTeamsPerGroup);

        for (int i = 0; i < tournament.getNumGroups(); i++) {
            String groupName = "" + (char) ('A' + i);
            List<Integer> groupTeams = new ArrayList<>(
                    teamIds.subList(i * numTeamsPerGroup, (i + 1) * numTeamsPerGroup));

            if (groupTeams.size() % 2 != 0) {
                groupTeams.add(null);
            }

            int n = groupTeams.size();
            int numDays = n - 1;
            int matchesPerDay = n / 2;

            System.out.println("[DEBUG - MatchService] - Generating matches for group " + groupName + " with teams "
                    + groupTeams);

            for (int day = 0; day < numDays; day++) {
                for (int match = 0; match < matchesPerDay; match++) {
                    int homeIndex = (day + match) % (n - 1);
                    int awayIndex = (day + n - 1 - match) % (n - 1);

                    if (match == 0) {
                        awayIndex = n - 1;
                    }

                    boolean random = Math.random() % 100 < 50;

                    Integer homeTeamId = random ? groupTeams.get(homeIndex) : groupTeams.get(awayIndex);
                    Integer awayTeamId = random ? groupTeams.get(awayIndex) : groupTeams.get(homeIndex);

                    if (homeTeamId != null && awayTeamId != null) {
                        Match matchEntity = new Match();
                        matchEntity.setTournament(tournament);
                        matchEntity.setGroupName(groupName);
                        System.out.println(
                                "[DEBUG - MatchService] - Generating match for group " + groupName + " with teams "
                                        + homeTeamId + " and " + awayTeamId);
                        if (tournament.getRound2GroupStage()) {
                            matchEntity.setLegOrder(LegOrderType.FIRST_LEG);
                        }
                        switch (sportCategory) {
                            case FOOTBALL -> matchEntity.setSportCategory(sportCategory);
                            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
                        }
                        Match savedMatch = saveMatch(matchEntity);

                        switch (sportCategory) {
                            case FOOTBALL -> {
                                final Optional<Team> homeTeamOptional = teamService.getTeamById(homeTeamId);
                                final Optional<Team> awayTeamOptional = teamService.getTeamById(awayTeamId);
                                if (homeTeamOptional.isEmpty() || awayTeamOptional.isEmpty()) {
                                    throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
                                }

                                FootballMatch footballMatch = new FootballMatch();
                                footballMatch.setMatch(savedMatch);
                                footballMatch.setHomeTeam(homeTeamOptional.get());
                                footballMatch.setAwayTeam(awayTeamOptional.get());
                                saveFootballMatch(footballMatch);
                            }
                        }

                        if (tournament.getRound2GroupStage()) {
                            Match matchEntity2 = new Match();
                            matchEntity2.setTournament(tournament);
                            matchEntity2.setGroupName(groupName);
                            matchEntity2.setLegOrder(LegOrderType.SECOND_LEG);
                            switch (sportCategory) {
                                case FOOTBALL -> matchEntity2.setSportCategory(sportCategory);
                                default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
                            }
                            Match savedMatch2 = saveMatch(matchEntity2);

                            switch (sportCategory) {
                                case FOOTBALL -> {
                                    final Optional<Team> homeTeamOptional = teamService.getTeamById(homeTeamId);
                                    final Optional<Team> awayTeamOptional = teamService.getTeamById(awayTeamId);
                                    if (homeTeamOptional.isEmpty() || awayTeamOptional.isEmpty()) {
                                        throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
                                    }

                                    FootballMatch footballMatch = new FootballMatch();
                                    footballMatch.setMatch(savedMatch2);
                                    footballMatch.setHomeTeam(awayTeamOptional.get());
                                    footballMatch.setAwayTeam(homeTeamOptional.get());
                                    saveFootballMatch(footballMatch);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateMatchesForFinalStage(Tournament tournament, SportCategory sportCategory) {
        List<KnockoutPhase> knockoutPhases = List.of(
                new KnockoutPhase(16, "OF", 8),
                new KnockoutPhase(8, "QF", 4),
                new KnockoutPhase(4, "SF", 2),
                new KnockoutPhase(2, "FI", 1));

        List<BracketStage> bracketStages = List.of(
                new BracketStage("QF1", "OF1", "OF2"),
                new BracketStage("QF2", "OF3", "OF4"),
                new BracketStage("QF3", "OF5", "OF6"),
                new BracketStage("QF4", "OF7", "OF8"),
                new BracketStage("SF1", "QF1", "QF2"),
                new BracketStage("SF2", "QF3", "QF4"),
                new BracketStage("FI", "SF1", "SF2"));

        Map<String, FinalStageBracket> finalStageBracketPerGroup = new HashMap<>();

        System.out.println(
                "[DEBUG - MatchService] - Generating matches for final stage of tournament " + tournament.getId()
                        + " with num Teams qualified " + tournament.getNumTeamsQualified() + " and sport category "
                        + sportCategory);

        for (KnockoutPhase knockoutPhase : knockoutPhases) {
            final int phaseQualified = knockoutPhase.qualified;
            final String phasePrefix = knockoutPhase.prefix;
            final int phaseMatches = knockoutPhase.matches;

            if (tournament.getNumTeamsQualified() >= phaseQualified) {
                for (int i = 1; i <= phaseMatches; i++) {
                    final String matchGroup = phaseMatches > 1 ? phasePrefix + i : phasePrefix;

                    FinalStageBracket finalStageBracket = new FinalStageBracket();
                    finalStageBracket.setTournament(tournament);
                    finalStageBracket.setGroupName(matchGroup);
                    finalStageBracket.setNextFinalStageBracketSlot(
                            phaseMatches > 1 ? (i % 2 == 1 ? MatchSlotType.HOME : MatchSlotType.AWAY) : null);
                    FinalStageBracket savedFinalStageBracket = saveFinalStageBracket(finalStageBracket);

                    finalStageBracketPerGroup.put(matchGroup, savedFinalStageBracket);

                    Match matchEntity = new Match();
                    matchEntity.setTournament(tournament);
                    matchEntity.setGroupName(matchGroup);
                    matchEntity.setFinalStageBracket(savedFinalStageBracket);
                    if (phaseQualified > 2 && tournament.getRound2FinalStage()) {
                        matchEntity.setLegOrder(LegOrderType.FIRST_LEG);
                    }
                    switch (sportCategory) {
                        case FOOTBALL -> matchEntity.setSportCategory(sportCategory);
                        default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
                    }
                    Match savedMatch = saveMatch(matchEntity);

                    switch (sportCategory) {
                        case FOOTBALL -> {
                            FootballMatch footballMatch = new FootballMatch();
                            footballMatch.setMatch(savedMatch);
                            saveFootballMatch(footballMatch);
                        }
                    }

                    if (phaseQualified > 2 && tournament.getRound2FinalStage()) {
                        Match matchEntity2 = new Match();
                        matchEntity2.setTournament(tournament);
                        matchEntity2.setGroupName(matchGroup);
                        matchEntity2.setFinalStageBracket(savedFinalStageBracket);
                        matchEntity2.setLegOrder(LegOrderType.SECOND_LEG);
                        switch (sportCategory) {
                            case FOOTBALL -> matchEntity2.setSportCategory(sportCategory);
                            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
                        }
                        Match savedMatch2 = saveMatch(matchEntity2);

                        switch (sportCategory) {
                            case FOOTBALL -> {
                                FootballMatch footballMatch = new FootballMatch();
                                footballMatch.setMatch(savedMatch2);
                                saveFootballMatch(footballMatch);
                            }
                        }
                    }

                    System.out.println("[DEBUG - MatchService] - Generated match for final stage with group "
                            + matchGroup + " for tournament " + tournament.getId() + " with final stage bracket "
                            + savedFinalStageBracket.getId());
                }
            }
        }
        for (BracketStage stage : bracketStages) {
            FinalStageBracket next = finalStageBracketPerGroup.get(stage.nextGroup());
            FinalStageBracket home = finalStageBracketPerGroup.get(stage.homeGroup());
            FinalStageBracket away = finalStageBracketPerGroup.get(stage.awayGroup());
            if (home != null) {
                home.setNextFinalStageBracket(next);
                saveFinalStageBracket(home);
            }
            if (away != null) {
                away.setNextFinalStageBracket(next);
                saveFinalStageBracket(away);
            }

            if (home != null && away != null) {
                System.out
                        .println("[DEBUG - MatchService] - Set next final stage bracket for group " + stage.homeGroup()
                                + " and " + stage.awayGroup() + " to " + stage.nextGroup() + " for tournament "
                                + tournament.getId());
            }

        }
    }

    public FinalStageBracket saveFinalStageBracket(FinalStageBracket finalStageBracket) {
        return finalStageBracketRepository.save(finalStageBracket);
    }

    public List<Integer> getTeamIdsByTournamentIdAndGroupStage(Integer tournamentId, String groupName) {
        Optional<Tournament> optionalTournament = tournamentService.getTournamentById(tournamentId);
        if (optionalTournament.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }

        System.out.println("[DEBUG - MatchService] - Retrieving team IDs for tournament " + tournamentId + " and group "
                + groupName);

        switch (optionalTournament.get().getSport().getSportCategory()) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = footballMatchRepository
                        .findByMatch_Tournament_IdAndMatch_GroupName(tournamentId, groupName);
                if (footballMatches.isEmpty()) {
                    throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
                }
                return footballMatches.stream()
                        .flatMap(match -> List.of(match.getHomeTeam().getId(), match.getAwayTeam().getId()).stream())
                        .distinct()
                        .toList();
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public void deleteAllMatches() {
        matchRepository.deleteAll();
        footballMatchRepository.deleteAll();
        finalStageBracketRepository.deleteAll();
    }

    public List<FootballMatch> getAssignedFootballMatchesByRefereeId(Integer refereeId) {
        List<RefereeAssignment> refereeAssignments = refereeAssignmentRepository.findByReferee_RefereeId(refereeId);
        List<FootballMatch> assignedFootballMatches = new ArrayList<>();
        for (RefereeAssignment refereeAssignment : refereeAssignments) {
            Match match = refereeAssignment.getMatch();
            if (match.getSportCategory() == SportCategory.FOOTBALL) {
                Optional<FootballMatch> footballMatchOptional = footballMatchRepository.findByMatch_Id(match.getId());
                footballMatchOptional.ifPresent(assignedFootballMatches::add);
            }
        }
        return assignedFootballMatches;
    }

    public List<Team> getTeamsByMatchId(Integer matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);
        if (matchOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
        }
        Match match = matchOptional.get();
        if (match.getSportCategory() == SportCategory.FOOTBALL) {
            Optional<FootballMatch> footballMatchOptional = footballMatchRepository.findByMatch_Id(matchId);
            if (footballMatchOptional.isEmpty()) {
                throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
            }
            FootballMatch footballMatch = footballMatchOptional.get();
            return List.of(footballMatch.getHomeTeam(), footballMatch.getAwayTeam());
        } else {
            throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    public List<FootballMatch> getScheduledFootballMatches() {
        return footballMatchRepository.findByStatus(FootballMatchStatus.SCHEDULED);
    }

    
}
