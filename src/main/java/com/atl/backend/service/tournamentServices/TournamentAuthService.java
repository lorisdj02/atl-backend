package com.atl.backend.service.tournamentServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.TournamentDTO;
import com.atl.backend.exception.ForbiddenException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Tournament;
import com.atl.backend.repository.TournamentRepository;

@Service
public class TournamentAuthService {
    @Autowired
    private TournamentRepository tournamentRepository;

    public Boolean canManageTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.TOURNAMENT_NOT_FOUND));
        if (AccountContext.isAdmin())
            return true;

        if (AccountContext.isCoordinator()) {
            return tournament.getCoordinator().getCoordinatorId().equals(AccountContext.getCoordinatorId());
        }

        return false;
    }

    public void checkManageTournament(Long tournamentId) {
        if (!canManageTournament(tournamentId))
            throw new UnauthorizedException(ErrorsString.YOU_MUST_BE_TOURNAMENT_COORDINATOR);
    }

    public Boolean canCreateTournament(TournamentDTO tournamentDTO) {
        if (AccountContext.isAdmin())
            return true;

        if (AccountContext.isCoordinator()) {
            return tournamentDTO.getCoordinatorId().equals(AccountContext.getCoordinatorId());
        }

        return false;
    }

    public void checkCreateTournament(TournamentDTO tournamentDTO) {
        if (!canCreateTournament(tournamentDTO))
            throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
    }
}
