package com.atl.backend.service.matchServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.FinalStageBracket;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.FootballStanding;
import com.atl.backend.model.Match;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.LegOrderType;
import com.atl.backend.model.enums.MatchSlotType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.FinalStageBracketRepository;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.repository.FootballStandingRepository;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.service.TeamService;

@Service
public class MatchCombinationService {
    @Autowired
    private TeamService teamService;

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private FootballMatchRepository footballMatchRepository;
    @Autowired
    private FinalStageBracketRepository finalStageBracketRepository;
    @Autowired
    private FootballStandingRepository footballStandingRepository;

    private Tournament getExistingTournamentByTournamentId(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
    }

    private record KnockoutPhase(int qualified, String prefix, int matches) {
    }

    private record BracketStage(String nextGroup, String homeGroup, String awayGroup) {
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateMatchesForTournament(Tournament tournament, List<Integer> teamIds) {

        if (!tournament.getCoordinator().getCoordinatorId().equals(AccountContext.getCoordinatorId())) {
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_TOURNAMENT_COORDINATOR);
        }

        System.out
                .println("[DEBUG - MatchService] - Retrieved team IDs for tournament " + tournament.getId() + ": " + teamIds);

        List<Integer> shuffledTeamIds = new ArrayList<>(teamIds);
        Collections.shuffle(shuffledTeamIds);

        System.out
                .println("[DEBUG - MatchService] - Teams participating in tournament " + tournament.getId() + ": " + shuffledTeamIds);

        final SportCategory sportCategory = tournament.getSport().getSportCategory();

        System.out.println("[DEBUG - MatchService] - Generating matches for group stage of tournament " + tournament.getId());

        generateMatchesForGroupStage(tournament, shuffledTeamIds, sportCategory);

        System.out.println("[DEBUG - MatchService] - Generating matches for final stage of tournament " + tournament.getId());

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
                        Match savedMatch = matchRepository.save(matchEntity);

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
                                footballMatchRepository.save(footballMatch);
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
                            Match savedMatch2 = matchRepository.save(matchEntity2);

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
                                    footballMatchRepository.save(footballMatch);
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
                    FinalStageBracket savedFinalStageBracket = finalStageBracketRepository.save(finalStageBracket);

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
                    Match savedMatch = matchRepository.save(matchEntity);

                    switch (sportCategory) {
                        case FOOTBALL -> {
                            FootballMatch footballMatch = new FootballMatch();
                            footballMatch.setMatch(savedMatch);
                            footballMatchRepository.save(footballMatch);
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
                        Match savedMatch2 = matchRepository.save(matchEntity2);

                        switch (sportCategory) {
                            case FOOTBALL -> {
                                FootballMatch footballMatch = new FootballMatch();
                                footballMatch.setMatch(savedMatch2);
                                footballMatchRepository.save(footballMatch);
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
                finalStageBracketRepository.save(home);
            }
            if (away != null) {
                away.setNextFinalStageBracket(next);
                finalStageBracketRepository.save(away);
            }

            if (home != null && away != null) {
                System.out
                        .println("[DEBUG - MatchService] - Set next final stage bracket for group " + stage.homeGroup()
                                + " and " + stage.awayGroup() + " to " + stage.nextGroup() + " for tournament "
                                + tournament.getId());
            }

        }
    }

    @Transactional(readOnly = true)
    private Boolean areTeamInSameGroup(Integer tournamentId, Integer teamId1, Integer teamId2) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);
        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL:
                Optional<FootballStanding> footballStandingOptional1 = footballStandingRepository
                        .findByTournament_IdAndTeam_Id(tournamentId, teamId1);
                Optional<FootballStanding> footballStandingOptional2 = footballStandingRepository
                        .findByTournament_IdAndTeam_Id(tournamentId, teamId2);
                if (footballStandingOptional1.isEmpty() || footballStandingOptional2.isEmpty()) {
                    throw new NotFoundException(ErrorsString.STANDING_NOT_FOUND);
                }
                FootballStanding footballStanding1 = footballStandingOptional1.get();
                FootballStanding footballStanding2 = footballStandingOptional2.get();
                return footballStanding1.getGroupName().equals(footballStanding2.getGroupName());
            default:
                throw new IllegalArgumentException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void drawKnockoutStageMatches(int tournamentId, Map<Integer, List<Team>> teamsPerRank) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);
        String groupNamePrefix;
        switch (tournament.getNumTeamsQualified()) {
            case 32 -> groupNamePrefix = "32";
            case 16 -> groupNamePrefix = "OF";
            case 8 -> groupNamePrefix = "QF";
            case 4 -> groupNamePrefix = "SF";
            case 2 -> groupNamePrefix = "FI";
            default -> throw new BadRequestException(ErrorsString.INVALID_NUMBER_OF_QUALIFIED_TEAMS);
        }

        List<Match> matches = matchRepository.findByTournament_Id(tournamentId);
        List<Match> knockoutStageMatches = matches.stream()
                .filter(match -> match.getGroupName() != null && match.getGroupName().startsWith(groupNamePrefix))
                .toList();

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = footballMatchRepository.findByMatch_Tournament_Id(tournamentId);

                // ===== STEP 1: Creare una lista unica di team ordinati dal migliore al
                // peggiore =====
                // Ordina i rank (già ordinati nel map, ma per sicurezza)
                List<Integer> sortedRanks = teamsPerRank.keySet().stream()
                        .sorted(Integer::compareTo)
                        .toList();

                // Colleziona tutti i team in ordine: i team del rank 1 (migliori), poi rank 2,
                // etc
                List<Team> allTeamsSorted = new ArrayList<>();
                for (Integer rank : sortedRanks) {
                    allTeamsSorted.addAll(teamsPerRank.get(rank));
                }

                // ===== STEP 2: Creare due liste: migliori (dal basso) e peggiori (dall'alto)
                // =====
                List<Team> bestTeams = new ArrayList<>(allTeamsSorted);
                List<Team> worstTeams = new ArrayList<>(allTeamsSorted);
                Collections.reverse(worstTeams); // Reverse per andare dal peggiore al migliore

                // ===== STEP 3: Tracciare i team già usati =====
                Set<Integer> usedTeams = new HashSet<>();

                // ===== STEP 4: Abbinare i match: best[i] vs worst[i] =====
                for (FootballMatch footballMatch : footballMatches) {
                    Match match = footballMatch.getMatch();

                    // Controllare se questo match è effettivamente un match knockout
                    if (match == null || !knockoutStageMatches.contains(match)) {
                        continue;
                    }

                    // Saltare se già assegnato
                    if (footballMatch.getHomeTeam() != null || footballMatch.getAwayTeam() != null) {
                        continue;
                    }

                    // Cercare il prossimo team non usato dalla lista dei migliori (homeTeam)
                    Team homeTeam = null;
                    for (Team team : bestTeams) {
                        if (!usedTeams.contains(team.getId())) {
                            homeTeam = team;
                            usedTeams.add(team.getId());
                            break;
                        }
                    }

                    // Se non troviamo un homeTeam, abbiamo finito
                    if (homeTeam == null) {
                        break;
                    }

                    // Cercare il prossimo team non usato dalla lista dei peggiori (awayTeam)
                    // IMPORTANTE: deve essere di diverso group stage
                    Team awayTeam = null;
                    for (Team team : worstTeams) {
                        if (!usedTeams.contains(team.getId()) &&
                                !areTeamInSameGroup(tournamentId, homeTeam.getId(), team.getId())) {
                            awayTeam = team;
                            usedTeams.add(team.getId());
                            break;
                        }
                    }

                    // Se trovato, assegnare al match
                    if (awayTeam != null) {
                        footballMatch.setHomeTeam(homeTeam);
                        footballMatch.setAwayTeam(awayTeam);
                    } else {
                        // Se non trovato awayTeam, togliere homeTeam dalla lista usati e riprovare
                        usedTeams.remove(homeTeam.getId());
                    }
                }

                // ===== STEP 5: Salvare tutti i match =====
                footballMatchRepository.saveAll(footballMatches);
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateKnockoutStageMatches(int tournamentId) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);
        String groupNamePrefix;

        switch (tournament.getPhase()) {
            case RO32 -> groupNamePrefix = "32";
            case RO16 -> groupNamePrefix = "OF";
            case QUARTER -> groupNamePrefix = "QF";
            case SEMI -> groupNamePrefix = "SF";
            case FINAL -> groupNamePrefix = "FI";
            default -> throw new BadRequestException(ErrorsString.INVALID_TOURNAMENT_PHASE);
        }

        if (groupNamePrefix.equals("FI")) {
            // Se siamo in finale, non esiste una fase successiva da controllare, possiamo
            // semplicemente ritornare
            return;
        }

        switch (tournament.getSport().getSportCategory()) {
            case FOOTBALL -> {
                List<FootballMatch> footballMatches = footballMatchRepository
                        .findByMatch_Tournament_IdAndGroupPrefixAndLegOrder(tournamentId, groupNamePrefix,
                                LegOrderType.FIRST_LEG);
                if (footballMatches.size() < 2 || footballMatches.isEmpty()) {
                    throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                }
                for (FootballMatch footballMatch : footballMatches) {
                    Match match = footballMatch.getMatch();
                    FinalStageBracket finalStageBracket = match.getFinalStageBracket();
                    if (finalStageBracket == null) {
                        throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                    }
                    FinalStageBracket nextFinalStageBracket = finalStageBracket.getNextFinalStageBracket();
                    if (nextFinalStageBracket == null) {
                        throw new NotFoundException(ErrorsString.MATCH_NOT_FOUND);
                    }
                    Team homeTeam = footballMatch.getHomeTeam();
                    Team awayTeam = footballMatch.getAwayTeam();

                    List<FootballMatch> nextFootballMatches = footballMatchRepository
                            .findByMatch_FinalStageBracket_Id(nextFinalStageBracket.getId());
                    for (FootballMatch nextFootballMatch : nextFootballMatches) {
                        if (finalStageBracket.getNextFinalStageBracketSlot() == MatchSlotType.HOME) {
                            nextFootballMatch.setHomeTeam(finalStageBracket.isHomeWinner() ? homeTeam
                                    : finalStageBracket.isAwayWinner() ? awayTeam : null);
                            footballMatchRepository.save(nextFootballMatch);
                            break;
                        } else if (finalStageBracket.getNextFinalStageBracketSlot() == MatchSlotType.AWAY) {
                            nextFootballMatch.setAwayTeam(finalStageBracket.isHomeWinner() ? homeTeam
                                    : finalStageBracket.isAwayWinner() ? awayTeam : null);
                            footballMatchRepository.save(nextFootballMatch);
                            break;
                        }
                    }
                }
            }
            default -> throw new BadRequestException(ErrorsString.UNSUPPORTED_SPORT_CATEGORY);
        }
    }

}
