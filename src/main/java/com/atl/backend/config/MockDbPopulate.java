package com.atl.backend.config;

import com.atl.backend.service.FieldService;
import com.atl.backend.service.SportService;
import com.atl.backend.service.TeamService;
import com.atl.backend.service.tournamentServices.TournamentParticipantService;
import com.atl.backend.service.tournamentServices.TournamentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.atl.backend.model.Account;
import com.atl.backend.model.City;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Field;
import com.atl.backend.model.GrowthStep;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.Sport;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.AccountRole;
import com.atl.backend.model.enums.GrowthStepType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.model.enums.TournamentPhase;
import com.atl.backend.model.enums.TournamentStatus;
import com.atl.backend.service.AccountService;

import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

@Component
public class MockDbPopulate {
    @Autowired
    private TournamentParticipantService tournamentParticipantService;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private SportService sportService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private DataSource dataSource;


    @Transactional
    public void createAccounts() {
        for (Integer i = 1; i <= 300; i++) {
            Account account = new Account();
            account.setEmail("email" + i.toString() + "@email.com");
            account.setName("name" + i.toString());
            account.setSurname("surname" + i.toString());
            account.setPhone("phone" + i.toString());
            account.setBirthdate(LocalDate.parse("2002-01-08"));
            if (i <= 200) {
                account.setUsername("player" + i.toString());
                account.setRole(AccountRole.PLAYER);
                accountService.registerAccount(account, null);
            } else if (i <= 250) {
                account.setUsername("coordinator" + i.toString());
                account.setRole(AccountRole.COORDINATOR);
                accountService.registerAccount(account, null);
            } else if (i <= 275) {
                account.setUsername("referee" + i.toString());
                account.setRole(AccountRole.REFEREE);
                accountService.registerAccount(account, null);
            } else {
                account.setUsername("organization" + i.toString());
                account.setRole(AccountRole.ORGANIZATION);
                accountService.registerAccount(account, Optional.of("orgName" + i.toString()));
            }
        }
        System.out.println("[DEBUG] - Created 200 players, 50 coordinators, 25 referees, 25 organizations");
    }

    @Transactional
    public void createTeams() {
        List<Integer> playerIds = accountService.getAllPlayers().stream().map(player -> player.getPlayerId()).toList();

        List<Team> teams = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            Team team = new Team();
            String alias = "TAA";
            team.setName("team" + i);
            team.setSportCategory(SportCategory.FOOTBALL);
            team.setCaptainPlayer(accountService.getPlayerByPlayerId(playerIds.get(i - 1)).get());
            team.setAlias(alias);
            Team savedTeam = teamService.saveTeam(team);
            teams.add(savedTeam);
            for (int j = 1; j <= 10; j++) {
                teamService.joinTeam(playerIds.get((i - 1) * 10 + j - 1), savedTeam.getId());
            }
        }

        System.out.println("[DEBUG] - Created 20 teams, each with 10 players");
    }

    @Transactional
    public void createSports() {
        Sport football5v5 = new Sport();
        football5v5.setId(1);
        football5v5.setName("Football 5v5");
        football5v5.setMinPlayersPerTeam((short) 5);
        football5v5.setMaxPlayersPerTeam((short) 10);
        football5v5.setMinutesPerRound((short) 20);
        football5v5.setMinutesPerPause((short) 5);
        football5v5.setSportCategory(SportCategory.FOOTBALL);
        football5v5.setMinReferee((short) 1);
        sportService.saveSport(football5v5);

        Sport football7v7 = new Sport();
        football7v7.setId(2);
        football7v7.setName("Football 7v7");
        football7v7.setMinPlayersPerTeam((short) 7);
        football7v7.setMaxPlayersPerTeam((short) 14);
        football7v7.setMinutesPerRound((short) 25);
        football7v7.setMinutesPerPause((short) 10);
        football7v7.setSportCategory(SportCategory.FOOTBALL);
        football7v7.setMinReferee((short) 1);
        sportService.saveSport(football7v7);

        Sport football8v8 = new Sport();
        football8v8.setId(3);
        football8v8.setName("Football 8v8");
        football8v8.setMinPlayersPerTeam((short) 8);
        football8v8.setMaxPlayersPerTeam((short) 16);
        football8v8.setMinutesPerRound((short) 30);
        football8v8.setMinutesPerPause((short) 10);
        football8v8.setSportCategory(SportCategory.FOOTBALL);
        football8v8.setMinReferee((short) 1);
        sportService.saveSport(football8v8);

        Sport football11v11 = new Sport();
        football11v11.setId(4);
        football11v11.setName("Football 11v11");
        football11v11.setMinPlayersPerTeam((short) 11);
        football11v11.setMaxPlayersPerTeam((short) 22);
        football11v11.setMinutesPerRound((short) 45);
        football11v11.setMinutesPerPause((short) 15);
        football11v11.setSportCategory(SportCategory.FOOTBALL);
        football11v11.setMinReferee((short) 3);
        sportService.saveSport(football11v11);

        System.out.println("[DEBUG] - Created 4 sports: Football 5v5, Football 7v7, Football 8v8, Football 11v11");
    }

    @Transactional
    public void createCities() {
        City milano = new City();
        milano.setId(1);
        milano.setName("Milano");
        fieldService.saveCity(milano);
        City roma = new City();
        roma.setId(2);
        roma.setName("Roma");
        fieldService.saveCity(roma);
        City taranto = new City();
        taranto.setId(3);
        taranto.setName("Taranto");
        fieldService.saveCity(taranto);
        System.out.println("[DEBUG] - Created 3 cities: Milano, Roma, Taranto");

    }

    public void createFields() {
        Map<SportCategory, Short> maxPlayersPerSportCategoryField1 = new HashMap<>();
        maxPlayersPerSportCategoryField1.put(SportCategory.FOOTBALL, (short) 10);
        List<Organization> organizations = accountService.getAllOrganizations();

        Field field1 = new Field();
        field1.setName("Campo 1");
        field1.setCity(fieldService.getCityById(1).get());
        field1.setLocationLat((double) 12);
        field1.setLocationLng((double) 13);
        field1.setAddress("Via Roma 1");
        field1.setOrganization(organizations.get(0));
        field1.setOpeningTime(LocalTime.of(8, 0));
        field1.setClosingTime(LocalTime.of(23, 0));
        field1.setHourCostPrivate(new BigDecimal("75"));
        field1.setHourCostTournament(new BigDecimal("50"));
        fieldService.saveField(field1, maxPlayersPerSportCategoryField1);

        Map<SportCategory, Short> maxPlayersPerSportCategoryField2 = new HashMap<>();
        maxPlayersPerSportCategoryField2.put(SportCategory.FOOTBALL, (short) 14);

        Field field2 = new Field();
        field2.setName("Campo 2");
        field2.setCity(fieldService.getCityById(2).get());
        field2.setLocationLat((double) 12);
        field2.setLocationLng((double) 13);
        field2.setAddress("Via Milano 2");
        field2.setOrganization(organizations.get(0));
        field2.setOpeningTime(LocalTime.of(8, 0));
        field2.setClosingTime(LocalTime.of(23, 0));
        field2.setHourCostPrivate(new BigDecimal("75"));
        field2.setHourCostTournament(new BigDecimal("50"));
        fieldService.saveField(field2, maxPlayersPerSportCategoryField2);

        Map<SportCategory, Short> maxPlayersPerSportCategoryField3 = new HashMap<>();
        maxPlayersPerSportCategoryField3.put(SportCategory.FOOTBALL, (short) 22);

        Field field3 = new Field();
        field3.setName("Campo 3");
        field3.setCity(fieldService.getCityById(3).get());
        field3.setLocationLat((double) 12);
        field3.setLocationLng((double) 13);
        field3.setAddress("Via Palermo 3");
        field3.setOrganization(organizations.get(0));
        field3.setOpeningTime(LocalTime.of(8, 0));
        field3.setClosingTime(LocalTime.of(23, 0));
        field3.setHourCostPrivate(new BigDecimal("100"));
        field3.setHourCostTournament(new BigDecimal("75"));
        fieldService.saveField(field3, maxPlayersPerSportCategoryField3);
        System.out.println("[DEBUG] - Created 3 fields: Campo 1, Campo 2, Campo 3");
    }

    public void createRefereeRoles() {
        RefereeRole role1 = new RefereeRole();
        role1.setSport(sportService.getSportById(1).get());
        role1.setName("MAIN");
        role1.setLevel((short) 1);
        role1.setPayPerMatch(new BigDecimal(15));
        sportService.saveRefereeRole(role1);

        RefereeRole role2 = new RefereeRole();
        role2.setSport(sportService.getSportById(1).get());
        role2.setName("MAIN");
        role2.setLevel((short) 2);
        role2.setPayPerMatch(new BigDecimal(20));
        sportService.saveRefereeRole(role2);

        RefereeRole role3 = new RefereeRole();
        role3.setSport(sportService.getSportById(1).get());
        role3.setName("MAIN");
        role3.setLevel((short) 3);
        role3.setPayPerMatch(new BigDecimal(25));
        sportService.saveRefereeRole(role3);

        RefereeRole role4 = new RefereeRole();
        role4.setSport(sportService.getSportById(1).get());
        role4.setName("MAIN");
        role4.setLevel((short) 4);
        role4.setPayPerMatch(new BigDecimal(30));
        sportService.saveRefereeRole(role4);

        RefereeRole role5 = new RefereeRole();
        role5.setSport(sportService.getSportById(2).get());
        role5.setName("MAIN");
        role5.setLevel((short) 1);
        role5.setPayPerMatch(new BigDecimal(18));
        sportService.saveRefereeRole(role5);

        RefereeRole role6 = new RefereeRole();
        role6.setSport(sportService.getSportById(2).get());
        role6.setName("MAIN");
        role6.setLevel((short) 2);
        role6.setPayPerMatch(new BigDecimal(22));
        sportService.saveRefereeRole(role6);

        RefereeRole role7 = new RefereeRole();
        role7.setSport(sportService.getSportById(2).get());
        role7.setName("MAIN");
        role7.setLevel((short) 3);
        role7.setPayPerMatch(new BigDecimal(28));
        sportService.saveRefereeRole(role7);

        RefereeRole role8 = new RefereeRole();
        role8.setSport(sportService.getSportById(2).get());
        role8.setName("MAIN");
        role8.setLevel((short) 4);
        role8.setPayPerMatch(new BigDecimal(35));
        sportService.saveRefereeRole(role8);

        RefereeRole role9 = new RefereeRole();
        role9.setSport(sportService.getSportById(3).get());
        role9.setName("MAIN");
        role9.setLevel((short) 1);
        role9.setPayPerMatch(new BigDecimal(20));
        sportService.saveRefereeRole(role9);

        RefereeRole role10 = new RefereeRole();
        role10.setSport(sportService.getSportById(3).get());
        role10.setName("MAIN");
        role10.setLevel((short) 2);
        role10.setPayPerMatch(new BigDecimal(25));
        sportService.saveRefereeRole(role10);

        RefereeRole role11 = new RefereeRole();
        role11.setSport(sportService.getSportById(3).get());
        role11.setName("MAIN");
        role11.setLevel((short) 3);
        role11.setPayPerMatch(new BigDecimal(30));
        sportService.saveRefereeRole(role11);

        RefereeRole role12 = new RefereeRole();
        role12.setSport(sportService.getSportById(3).get());
        role12.setName("MAIN");
        role12.setLevel((short) 4);
        role12.setPayPerMatch(new BigDecimal(35));
        sportService.saveRefereeRole(role12);

        RefereeRole role13 = new RefereeRole();
        role13.setSport(sportService.getSportById(4).get());
        role13.setName("MAIN");
        role13.setLevel((short) 1);
        role13.setPayPerMatch(new BigDecimal(30));
        sportService.saveRefereeRole(role13);

        RefereeRole role14 = new RefereeRole();
        role14.setSport(sportService.getSportById(4).get());
        role14.setName("MAIN");
        role14.setLevel((short) 2);
        role14.setPayPerMatch(new BigDecimal(35));
        sportService.saveRefereeRole(role14);

        RefereeRole role15 = new RefereeRole();
        role15.setSport(sportService.getSportById(4).get());
        role15.setName("MAIN");
        role15.setLevel((short) 3);
        role15.setPayPerMatch(new BigDecimal(40));
        sportService.saveRefereeRole(role15);

        RefereeRole role16 = new RefereeRole();
        role16.setSport(sportService.getSportById(4).get());
        role16.setName("MAIN");
        role16.setLevel((short) 4);
        role16.setPayPerMatch(new BigDecimal(50));
        sportService.saveRefereeRole(role16);

        RefereeRole role17 = new RefereeRole();
        role17.setSport(sportService.getSportById(4).get());
        role17.setName("ASSISTANT1");
        role17.setLevel((short) 1);
        role17.setPayPerMatch(new BigDecimal(15));
        sportService.saveRefereeRole(role17);

        RefereeRole role18 = new RefereeRole();
        role18.setSport(sportService.getSportById(4).get());
        role18.setName("ASSISTANT1");
        role18.setLevel((short) 2);
        role18.setPayPerMatch(new BigDecimal(18));
        sportService.saveRefereeRole(role18);

        RefereeRole role19 = new RefereeRole();
        role19.setSport(sportService.getSportById(4).get());
        role19.setName("ASSISTANT1");
        role19.setLevel((short) 3);
        role19.setPayPerMatch(new BigDecimal(20));
        sportService.saveRefereeRole(role19);

        RefereeRole role20 = new RefereeRole();
        role20.setSport(sportService.getSportById(4).get());
        role20.setName("ASSISTANT1");
        role20.setLevel((short) 4);
        role20.setPayPerMatch(new BigDecimal(25));
        sportService.saveRefereeRole(role20);

        RefereeRole role21 = new RefereeRole();
        role21.setSport(sportService.getSportById(4).get());
        role21.setName("ASSISTANT2");
        role21.setLevel((short) 1);
        role21.setPayPerMatch(new BigDecimal(15));
        sportService.saveRefereeRole(role21);

        RefereeRole role22 = new RefereeRole();
        role22.setSport(sportService.getSportById(4).get());
        role22.setName("ASSISTANT2");
        role22.setLevel((short) 2);
        role22.setPayPerMatch(new BigDecimal(18));
        sportService.saveRefereeRole(role22);

        RefereeRole role23 = new RefereeRole();
        role23.setSport(sportService.getSportById(4).get());
        role23.setName("ASSISTANT2");
        role23.setLevel((short) 3);
        role23.setPayPerMatch(new BigDecimal(20));
        sportService.saveRefereeRole(role23);

        RefereeRole role24 = new RefereeRole();
        role24.setSport(sportService.getSportById(4).get());
        role24.setName("ASSISTANT2");
        role24.setLevel((short) 4);
        role24.setPayPerMatch(new BigDecimal(25));
        sportService.saveRefereeRole(role24);
    }

    public void createGrowthSteps() {
        GrowthStep step1 = new GrowthStep();
        step1.setType(GrowthStepType.REFEREE);
        step1.setTitle("Amateur Referee");
        step1.setLevel((short) 1);
        step1.setX(0);
        step1.setMinRatingOverLastXMatches(BigDecimal.valueOf(0));
        step1.setMinRefereedMatches((short) 0);
        accountService.saveGrowthStep(step1);

        GrowthStep step2 = new GrowthStep();
        step2.setType(GrowthStepType.REFEREE);
        step2.setTitle("Pro Referee");
        step2.setLevel((short) 2);
        step2.setX(10);
        step2.setMinRatingOverLastXMatches(BigDecimal.valueOf(4.0));
        step2.setMinRefereedMatches((short) 30);
        accountService.saveGrowthStep(step2);

        GrowthStep step3 = new GrowthStep();
        step3.setType(GrowthStepType.REFEREE);
        step3.setTitle("Elite Referee");
        step3.setLevel((short) 3);
        step3.setX(20);
        step3.setMinRatingOverLastXMatches(BigDecimal.valueOf(4.5));
        step3.setMinRefereedMatches((short) 60);
        accountService.saveGrowthStep(step3);

        GrowthStep step4 = new GrowthStep();
        step4.setType(GrowthStepType.REFEREE);
        step4.setTitle("ATL Referee");
        step4.setLevel((short) 4);
        step4.setX(30);
        step4.setMinRatingOverLastXMatches(BigDecimal.valueOf(4.8));
        step4.setMinRefereedMatches((short) 100);
        accountService.saveGrowthStep(step4);

        GrowthStep step5 = new GrowthStep();
        step5.setType(GrowthStepType.COORDINATOR);
        step5.setTitle("Amateur Coordinator");
        step5.setLevel((short) 1);
        step5.setX(0);
        step5.setMinRatingOverLastXTournaments(BigDecimal.valueOf(0));
        step5.setMinCoordinatedTournaments((short) 0);
        step5.setCoordinatorPayPerMatch(BigDecimal.valueOf(12.5));
        accountService.saveGrowthStep(step5);

        GrowthStep step6 = new GrowthStep();
        step6.setType(GrowthStepType.COORDINATOR);
        step6.setTitle("Pro Coordinator");
        step6.setLevel((short) 2);
        step6.setX(3);
        step6.setMinRatingOverLastXTournaments(BigDecimal.valueOf(3.5));
        step6.setMinCoordinatedTournaments((short) 3);
        step6.setCoordinatorPayPerMatch(BigDecimal.valueOf(15));
        accountService.saveGrowthStep(step6);

        GrowthStep step7 = new GrowthStep();
        step7.setType(GrowthStepType.COORDINATOR);
        step7.setTitle("Elite Coordinator");
        step7.setLevel((short) 3);
        step7.setX(5);
        step7.setMinRatingOverLastXTournaments(BigDecimal.valueOf(4.0));
        step7.setMinCoordinatedTournaments((short) 10);
        step7.setCoordinatorPayPerMatch(BigDecimal.valueOf(17.5));
        accountService.saveGrowthStep(step7);

        GrowthStep step8 = new GrowthStep();
        step8.setType(GrowthStepType.COORDINATOR);
        step8.setTitle("ATL Coordinator");
        step8.setLevel((short) 4);
        step8.setX(10);
        step8.setMinRatingOverLastXTournaments(BigDecimal.valueOf(4.5));
        step8.setMinCoordinatedTournaments((short) 20);
        step8.setCoordinatorPayPerMatch(BigDecimal.valueOf(20));
        accountService.saveGrowthStep(step8);
    }

    @Transactional
    public void createTournaments() {
        List<Coordinator> coordinators = accountService.getAllCoordinators();
        List<Integer> fieldIds = fieldService.getAllFields().stream().map(field -> field.getId()).toList();
        List<Integer> teamIds = teamService.getAllTeams().stream().map(team -> team.getId()).toList();

        Tournament tournament = new Tournament();
        tournament.setName("Torneo in corso");
        tournament.setSport(sportService.getSportById(2).get());
        tournament.setStatus(TournamentStatus.CREATED);
        tournament.setCity(fieldService.getFieldById(fieldIds.get(0)).get().getCity());
        tournament.setStartDate(null);
        tournament.setCoordinator(coordinators.get(0));
        tournament.setField(fieldService.getFieldById(fieldIds.get(0)).get());
        tournament.setFee(new BigDecimal(175));
        tournament.setPayCoordinator(new BigDecimal(350));
        tournament.setPrize1(new BigDecimal(500));
        tournament.setNumGroups((short) 2);
        tournament.setNumTeams((short) 8);
        tournament.setNumTeamsQualified((short) 4);
        tournament.setDescription("Viva la cacca");
        tournament.setPhase(TournamentPhase.GROUP);
        tournament.setPaidTeams((short) 8);
        Tournament savedTournament1 = tournamentService.saveTournament(tournament);

        System.out.println("[DEBUG] - Created tournament 'Torneo in corso' with 8 teams, all paid");

        for (int i = 1; i <= 8; i++) {
            List<Player> players = teamService.getPlayersByTeamId(teamIds.get(i - 1));
            List<Integer> playerIds = players.stream().map(player -> player.getPlayerId()).toList();
            tournamentParticipantService.participateToTournament(savedTournament1.getId(), teamIds.get(i - 1), playerIds);
        }

        System.out.println("[DEBUG] - All 8 teams participated to 'Torneo in corso'");

        tournamentService.startTournament(savedTournament1.getId());

        System.out.println("[DEBUG] - Started tournament 'Torneo in corso'");

        Tournament tournament2 = new Tournament();
        tournament2.setName("Torneo da cominciare");
        tournament2.setSport(sportService.getSportById(1).get());
        tournament2.setStatus(TournamentStatus.CREATED);
        tournament2.setCity(fieldService.getFieldById(fieldIds.get(0)).get().getCity());
        tournament2.setStartDate(null);
        tournament2.setCoordinator(coordinators.get(1));
        tournament2.setField(fieldService.getFieldById(fieldIds.get(0)).get());
        tournament2.setFee(new BigDecimal(175));
        tournament2.setPayCoordinator(new BigDecimal(350));
        tournament2.setPrize1(new BigDecimal(500));
        tournament2.setNumGroups((short) 2);
        tournament2.setNumTeams((short) 10);
        tournament2.setNumTeamsQualified((short) 8);
        tournament2.setDescription("Viva la cacca da cominciare");
        tournament2.setPhase(TournamentPhase.GROUP);
        tournament2.setPaidTeams((short) 9);
        Tournament savedTournament2 = tournamentService.saveTournament(tournament2);

        System.out.println("[DEBUG] - Created tournament 'Torneo da cominciare' with 9 teams, all paid");

        for (int i = 1; i <= 8; i++) {
            List<Player> players = teamService.getPlayersByTeamId(teamIds.get(i + 8 - 1));
            List<Integer> playerIds = players.stream().map(player -> player.getPlayerId()).toList();
            tournamentParticipantService.participateToTournament(savedTournament2.getId(), teamIds.get(i + 8 - 1), playerIds);
        }
        System.out.println("[DEBUG] - Created tournament 'Torneo da cominciare' with 9 teams, all paid");
    }

    @Transactional
    public void clearDb() {
        // Temporarily disable FK checks in PostgreSQL
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("SET session_replication_role = 'replica'");
        } catch (SQLException e) {
            System.out.println("[DEBUG] - Warning: unable to disable FK checks: " + e.getMessage());
        }

        // perform deletions (order doesn't matter while FK checks disabled)
        tournamentService.deleteAllTournaments();
        fieldService.deleteAllFields();
        teamService.deleteAllTeams();
        accountService.deleteAllAccounts();
        sportService.deleteAllSports();
        sportService.deleteAllRefereeRoles();
        accountService.deleteAllGrowthSteps();

        // restore FK checks
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("SET session_replication_role = 'origin'");
        } catch (SQLException e) {
            System.out.println("[DEBUG] - Warning: unable to restore FK checks: " + e.getMessage());
        }

        // quick verification
        boolean ok = tournamentService.getAllTournaments().isEmpty()
                && fieldService.getAllFields().isEmpty()
                && teamService.getAllTeams().isEmpty()
                && accountService.getAllAccounts().isEmpty()
                && sportService.getAllSports().isEmpty()
                && sportService.getAllRefereeRoles().isEmpty()
                && accountService.getAllGrowthSteps().isEmpty();
        if (!ok) {
            System.out.println("[DEBUG] - clearDb: verification FAILED, some tables are not empty");
        } else {
            System.out.println("[DEBUG] - clearDb: verification OK, all target tables empty");
        }
    }

    @Transactional
    public void populateDb() {
        createSports();
        createAccounts();
        createRefereeRoles();
        createGrowthSteps();
        createTeams();
        createCities();
        createFields();
        createTournaments();
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
            for (String arg : args) {
                if (arg.equals("clear")) {
                    System.out.println("[DEBUG] - Clearing database...");
                    clearDb();
                    return;
                } else if (arg.equals("populate")) {
                    System.out.println("[DEBUG] - Clearing database...");
                    clearDb();
                    System.out.println("[DEBUG] - Populating database...");
                    populateDb();
                    return;
                }
            }

        };
    }
}
