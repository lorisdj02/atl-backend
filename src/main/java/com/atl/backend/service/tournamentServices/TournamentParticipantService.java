package com.atl.backend.service.tournamentServices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.TournamentParticipationDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Callup;
import com.atl.backend.model.Player;
import com.atl.backend.model.Team;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.TournamentParticipation;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.model.enums.TournamentStatus;
import com.atl.backend.repository.CallupRepository;
import com.atl.backend.repository.PlayerRepository;
import com.atl.backend.repository.TournamentParticipationRepository;
import com.atl.backend.repository.TournamentRepository;
import com.atl.backend.request.ConfirmCallupsRequest;
import com.atl.backend.service.TeamService;

@Service
public class TournamentParticipantService {

    @Autowired
    private TournamentParticipationRepository tournamentParticipationRepository;
    @Autowired
    private CallupRepository callupRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamService teamService;

    private Player getExistingPlayerByPlayerId(Integer playerId) {
        Optional<Player> playerOpt = playerRepository.findByPlayerId(playerId);
        if (playerOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return playerOpt.get();
    }

    private Player getExistingPlayerByAccountId(Integer accountId) {
        Optional<Player> playerOpt = playerRepository.findByAccount_Id(accountId);
        if (playerOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.PLAYER_NOT_FOUND);
        }
        return playerOpt.get();
    }

    private Tournament getExistingTournamentByTournamentId(Integer tournamentId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        return tournamentOptional.get();
    }

    public void deleteAllTournamentParticipations() {
        tournamentParticipationRepository.deleteAll();
    }

    public void deleteAllCallups() {
        callupRepository.deleteAll();
    }

    public List<Tournament> getTournamentsByTeamId(Integer teamId) {
        List<TournamentParticipation> participations = tournamentParticipationRepository.findByTeam_Id(teamId);
        return participations.stream().map(TournamentParticipation::getTournament).toList();
    }

    public List<TournamentParticipation> getTournamentParticipationsByTeamId(Integer teamId) {
        return tournamentParticipationRepository.findByTeam_Id(teamId);
    }

    public List<TournamentParticipation> getTournamentParticipationsByTournamentId(Integer tournamentId) {
        return tournamentParticipationRepository.findByTournament_Id(tournamentId);
    }

    public Optional<TournamentParticipation> getTournamentParticipationByTeamIdAndTournamentId(Integer teamId,
            Integer tournamentId) {
        return tournamentParticipationRepository.findByTeam_IdAndTournament_Id(teamId, tournamentId);
    }

    public TournamentParticipation saveTournamentParticipation(TournamentParticipation tournamentParticipation) {
        return tournamentParticipationRepository.save(tournamentParticipation);
    }

    public List<TournamentParticipation> getPaidTournamentParticipationsByTeamId(Integer teamId) {
        return tournamentParticipationRepository.findByTeam_IdAndToPay(teamId, BigDecimal.ZERO);
    }

    public List<TournamentParticipation> getNotPaidTournamentParticipationsByTeamId(Integer teamId) {
        return tournamentParticipationRepository.findByTeam_IdAndToPayGreaterThan(teamId, BigDecimal.ZERO);
    }

    public List<TournamentParticipation> getTeamParticipations(Integer teamId) {
        teamService.checkTeamExists(teamId);
        List<TournamentParticipation> participations = tournamentParticipationRepository.findByTeam_Id(teamId);
        return participations;
    }

    public Integer countTournamentsByPlayerIdAndSportCategory(Integer playerId, SportCategory sportCategory) {
        List<Callup> callups = callupRepository.findByPlayer_PlayerId(playerId);
        List<Callup> filteredCallups = callups.stream()
                .filter(callup -> callup.getTournament().getSport().getSportCategory() == sportCategory)
                .toList();
        return filteredCallups.size();
    }

    public boolean isPlayerCalledUpInTournament(Integer playerId, Integer tournamentId) {
        Optional<Callup> callupOptional = callupRepository.findByPlayer_PlayerIdAndTournament_Id(playerId,
                tournamentId);
        return callupOptional.isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public TournamentParticipation participateToTournament(Integer tournamentId, Integer teamId,
            List<Integer> playerIds) {
        Tournament tournament = getExistingTournamentByTournamentId(tournamentId);
        if (tournament.getStatus() != TournamentStatus.CREATED) {
            throw new ConflictException(ErrorsString.TOURNAMENT_ALREADY_STARTED);
        }

        if (tournament.getNumTeams() == tournament.getPaidTeams()) {
            throw new ConflictException(ErrorsString.TOURNAMENT_ALREADY_FULL);
        }

        teamService.checkTeamExists(teamId);
        Team team = teamService.getTeamById(teamId).get();

        if (!team.getSportCategory().equals(tournament.getSport().getSportCategory())) {
            throw new ConflictException(ErrorsString.TEAM_SPORT_CATEGORY_MUST_MATCH_TOURNAMENT_SPORT_CATEGORY);
        }

        checkValidCallups(teamId, playerIds);

        System.out.println("[DEBUG - TournamentService] - Participating team " + teamId + " to tournament "
                + tournamentId + " with players " + playerIds);

        Optional<TournamentParticipation> tournamentParticipationOptional = getTournamentParticipationByTeamIdAndTournamentId(
                teamId, tournamentId);

        TournamentParticipation tournamentParticipation = null;
        if (tournamentParticipationOptional.isPresent()) {
            callupRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
            tournamentParticipation = tournamentParticipationOptional.get();
        } else {
            System.out.println("[DEBUG - TournamentService] - Creating new tournament participation for team " + teamId
                    + " and tournament " + tournamentId);
            tournamentParticipation = new TournamentParticipation();
            tournamentParticipation.setTeam(team);
            tournamentParticipation.setTournament(tournament);
            tournamentParticipation.setToPay(tournament.getFee());
            tournamentParticipationRepository.save(tournamentParticipation);
            System.out.println("[DEBUG - TournamentService] - New tournament participation created for team " + teamId
                    + " and tournament " + tournamentId);
        }
        List<Callup> newCallups = new ArrayList<>();
        for (Integer playerId : playerIds) {
            Player player = getExistingPlayerByPlayerId(playerId);
            Callup callup = new Callup();
            callup.setPlayer(player);
            callup.setTournament(tournament);
            callup.setTeam(team);
            callupRepository.save(callup);
            newCallups.add(callup);
        }
        return tournamentParticipation;
    }

    public void checkValidCallups(Integer teamId, List<Integer> playerIds) {
        teamService.checkTeamExists(teamId);

        teamService.needToBeCaptainOrViceCaptain(teamId, AccountContext.getActivePlayer().getPlayerId());
        Boolean areCallupsValid = false;
        for (Integer playerId : playerIds) {
            if (!teamService.isPlayerInTeam(teamId, playerId)) {
                throw new ConflictException(ErrorsString.PLAYER_NOT_IN_TEAM);
            }
            areCallupsValid = teamService.isPlayerCaptainOrViceCaptain(playerId, teamId);
        }

        if (!areCallupsValid) {
            throw new ConflictException(ErrorsString.CAPTAIN_OR_ONE_VICE_CAPTAIN_MUST_BE_CALLED_UP);
        }
    }

    public List<Integer> getTeamIdsParticipatingInTournament(Integer tournamentId) {
        List<TournamentParticipation> participations = getTournamentParticipationsByTournamentId(tournamentId);
        return participations.stream().map(tp -> tp.getTeam().getId()).toList();
    }

    public List<Integer> getCalluppedPlayerIdsByTournamentIdAndTeamId(Integer tournamentId, Integer teamId) {
        List<Callup> callups = callupRepository.findByTournament_IdAndTeam_Id(tournamentId, teamId);
        return callups.stream().map(callup -> callup.getPlayer().getPlayerId()).toList();
    }

    public List<Integer> getCalluppedPlayerIdsByTournamentId(Integer tournamentId) {
        List<Callup> callups = callupRepository.findByTournament_Id(tournamentId);
        return callups.stream().map(callup -> callup.getPlayer().getPlayerId()).toList();
    }

    public List<Callup> getCallupsByPlayerId(Integer playerId) {
        return callupRepository.findByPlayer_PlayerId(playerId);
    }

    public Optional<Callup> getCallupByPlayerIdAndTournamentId(Integer playerId, Integer tournamentId) {
        return callupRepository.findByPlayer_PlayerIdAndTournament_Id(playerId, tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TournamentParticipation confirmCallups(Integer tournamentId, ConfirmCallupsRequest request) {
        if (!tournamentId.equals(request.getTournamentId())) {
            throw new BadRequestException(ErrorsString.TOURNAMENT_ID_MISMATCH);
        }
        return participateToTournament(request.getTournamentId(), request.getTeamId(), request.getSelectedPlayerIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCallupsCheck(Integer tournamentId, Integer teamId) {
        // TODO: Non sono sicuro che funzioni... tanto è tutto da testare, fare
        // attenzione
        teamService.checkTeamExists(teamId);
        teamService.needToBeCaptainOrViceCaptain(teamId, AccountContext.getPlayerId());

        getExistingTournamentByTournamentId(tournamentId);
        Optional<TournamentParticipation> tournamentParticipationOptional = tournamentParticipationRepository
                .findByTeam_IdAndTournament_Id(teamId, tournamentId);
        if (!tournamentParticipationOptional.isPresent()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_PARTICIPATION_NOT_FOUND);
        }
        if (tournamentParticipationOptional.get().getToPay().compareTo(BigDecimal.ZERO) == 0) {
            throw new ConflictException(ErrorsString.TOURNAMENT_PARTICIPATION_ALREADY_PAID);
        }
    }

    public void deleteCallupsByTeamIdIdAndTournamentId(Integer teamId, Integer tournamentId) {
        callupRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTournamentParticipationByTeamIdAndTournamentId(Integer teamId, Integer tournamentId) {
        callupRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
        tournamentParticipationRepository.deleteByTeam_IdAndTournament_Id(teamId, tournamentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void decreseTournamentParticipation_ToPay(Integer tournamentId, Integer accountId, BigDecimal amount) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND);
        }
        Tournament tournament = tournamentOptional.get();

        Player player = getExistingPlayerByAccountId(accountId);

        Optional<Team> teamOptional = teamService.getTeamByPlayerIdAndSportCategory(player.getPlayerId(),
                tournament.getSport().getSportCategory());
        if (teamOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TEAM_NOT_FOUND);
        }
        Team team = teamOptional.get();

        Optional<TournamentParticipation> tournamentParticipationOptional = getTournamentParticipationByTeamIdAndTournamentId(
                team.getId(), tournamentId);
        if (tournamentParticipationOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.TOURNAMENT_PARTICIPATION_NOT_FOUND);
        }
        TournamentParticipation tournamentParticipation = tournamentParticipationOptional.get();
        if (tournamentParticipation.getToPay().compareTo(amount) < 0) {
            throw new BadRequestException(ErrorsString.PAYMENT_AMOUNT_EXCEEDS_TO_PAY);
        }
        tournamentParticipation.setToPay(tournamentParticipation.getToPay().subtract(amount));
        saveTournamentParticipation(tournamentParticipation);
    }

    public List<TournamentParticipationDTO> getNotPaidTeamParticipations(Integer teamId) {
        teamService.checkTeamExists(teamId);
        return getTeamParticipations(teamId).stream()
                .filter(participation -> participation.getToPay().compareTo(BigDecimal.ZERO) > 0)
                .map(TournamentParticipationDTO::fromTournamentParticipation).toList();
    }
}
