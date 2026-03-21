package com.atl.backend.config;

import com.atl.backend.service.FieldService;
import com.atl.backend.service.SportService;
import com.atl.backend.service.TeamService;

import com.atl.backend.service.TournamentService;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.atl.backend.model.Account;
import com.atl.backend.model.City;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Field;
import com.atl.backend.model.FieldCategory;
import com.atl.backend.model.Organization;
import com.atl.backend.model.Player;
import com.atl.backend.model.PlaysAt;
import com.atl.backend.model.Sport;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.AccountRole;
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

    private final FieldService fieldService;
    private final TournamentService tournamentService;
    private final SportService sportService;
    private final TeamService teamService;
    private final AccountService accountService;
    private final DataSource dataSource;

    public MockDbPopulate(AccountService accountService, TeamService teamService, SportService sportService,
            TournamentService tournamentService, FieldService fieldService, DataSource dataSource) {
        this.accountService = accountService;
        this.teamService = teamService;
        this.sportService = sportService;
        this.tournamentService = tournamentService;
        this.fieldService = fieldService;
        this.dataSource = dataSource;
    }

    @Transactional
    public void createAccounts() {
        for (Integer i = 1; i <= 300; i++) {
            Account account = new Account();
            account.setEmail("email" + i.toString() + "@email.com");
            account.setName("name" + i.toString());
            account.setSurname("surname" + i.toString());
            account.setPhone("phone" + i.toString());
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
                tournamentService.participateToTournament(savedTournament1.getId(), teamIds.get(i - 1), playerIds);
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
                tournamentService.participateToTournament(savedTournament2.getId(), teamIds.get(i + 8 - 1), playerIds);
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
                && sportService.getAllSports().isEmpty();
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
