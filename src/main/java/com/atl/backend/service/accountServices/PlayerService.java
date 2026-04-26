package com.atl.backend.service.accountServices;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.dto.AccountDTO;
import com.atl.backend.model.Account;
import com.atl.backend.model.Player;
import com.atl.backend.model.Team;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.response.LoginResponse;
import com.atl.backend.service.bookingServices.BookingService;
import com.atl.backend.service.orchestrator.TournamentTeamMatchOrchestrator;
import com.atl.backend.service.teamServices.TeamService;

@Service
public class PlayerService {
    @Autowired
    private TeamService teamService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private TournamentTeamMatchOrchestrator tournamentTeamMatchOrchestrator;
    @Autowired
    private PlayerRepository playerRepository;

    public void deleteAllPlayers(){
        playerRepository.deleteAll();
    }

    public void deletePlayerByAccountId(Long accountId) {
        playerRepository.deleteByAccount_Id(accountId);
    }

    public Optional<Player> getPlayerByAccountId(Long accountId) {
        return playerRepository.findByAccount_Id(accountId);
    }

    public Optional<Player> getPlayerByPlayerId(Long playerId) {
        return playerRepository.findByPlayerId(playerId);
    }

    public LoginResponse handlePlayerLogin() {
        Player player = AccountContext.getActivePlayer();
        Optional<Team> teamOpt = teamService.getTeamByPlayerIdAndSportCategory(AccountContext.getPlayerId(),
                player.getAccount().getFavoriteSportCategory());
        return LoginResponse.builder()
                .account(AccountDTO.fromAccount(player.getAccount()))
                .roleId(AccountContext.getPlayerId())
                .teamId(teamOpt.isPresent() ? teamOpt.get().getId() : null)
                .build();
    }

    public void deletePlayerAndRelatedData() {
        tournamentTeamMatchOrchestrator.leaveAllTeams();
        bookingService.leaveAllBookings();
    }

    public Player registerPlayer(Account account) {
        Player player = new Player();
        player.setAccount(account);
        player.setPoints(0);
        return playerRepository.save(player);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
}
