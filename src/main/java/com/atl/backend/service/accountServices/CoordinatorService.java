package com.atl.backend.service.accountServices;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.Tournament;
import com.atl.backend.repository.CoordinatorRepository;
import com.atl.backend.repository.GrowthStepRepository;
import com.atl.backend.repository.TournamentRepository;

@Service
public class CoordinatorService {
    @Autowired
    private CoordinatorRepository coordinatorRepository;
    @Autowired
    private GrowthStepRepository growthStepRepository;
    @Autowired
    private TournamentRepository tournamentRepository;

    public Coordinator registerCoordinator(Account account) {
        Coordinator coordinator = new Coordinator();
        coordinator.setAccount(account);
        coordinator.setLevel((short) 0);
        return coordinatorRepository.save(coordinator);
    }

    public Short getCoordinatorLevelByAccountId(Integer accountId) {
        Coordinator coordinator = coordinatorRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND));
        return coordinator.getLevel();
    }

    public void deleteAllCoordinators() {
        coordinatorRepository.deleteAll();
    }

    public void deleteCoordinatorByAccountId(Integer accountId) {
        coordinatorRepository.deleteByAccount_Id(accountId);
    }

    public Optional<Coordinator> getCoordinatorByAccountId(Integer accountId) {
        return coordinatorRepository.findByAccount_Id(accountId);
    }

    public Optional<Coordinator> getCoordinatorByCoordinatorId(Integer coordinatorId) {
        return coordinatorRepository.findById(coordinatorId);
    }

    public List<Coordinator> getAllCoordinators() {
        return coordinatorRepository.findAll();
    }

    public BigDecimal getCoordinatorPayPerMatchByCoordinatorId(Integer coordinatorId) {
        Optional<Coordinator> coordinatorOptional = getCoordinatorByCoordinatorId(coordinatorId);
        if (coordinatorOptional.isEmpty()) {
            throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
        }
        Coordinator coordinator = coordinatorOptional.get();
        Short level = coordinator.getLevel();
        if (level == null) {
            level = 1; // Default level if not set
        }

        BigDecimal pay = growthStepRepository.findCoordinatorPayPerMatchByLevel(level);
        return pay;
    }

    public void deleteCoordinatorAndRelatedData() {
        Coordinator coordinator = AccountContext.getActiveCoordinator();
        if (hasCoordinatorActiveTournamentsByCoordinatorId(coordinator.getCoordinatorId())) {
            throw new ConflictException(ErrorsString.COORDINATOR_HAS_ACTIVE_TOURNAMENTS);
        }
    }

    public Boolean hasCoordinatorActiveTournamentsByCoordinatorId(Integer coordinatorId) {
        List<Tournament> activeTournaments = tournamentRepository
                .findByCoordinator_CoordinatorId(coordinatorId);
        for (Tournament tournament : activeTournaments) {
            if (tournament.isTournamentActive()) {
                return true;
            }
        }
        return false;
    }

}
