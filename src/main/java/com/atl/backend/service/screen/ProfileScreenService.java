package com.atl.backend.service.screen;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;

import com.atl.backend.dto.GrowthStepDTO;
import com.atl.backend.model.Account;
import com.atl.backend.model.Coordinator;
import com.atl.backend.model.GrowthStep;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.Sport;
import com.atl.backend.model.enums.GrowthStepType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.response.MyGrowthResponse;
import com.atl.backend.service.AccountService;
import com.atl.backend.service.SportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.exception.*;

@Service
public class ProfileScreenService {
    
    @Autowired
    private  AccountService accountService;
    @Autowired
    private  SportService sportService;


    @Transactional(readOnly = true)
    public MyGrowthResponse getMyGrowthScreen() {
        Integer accountId = AccountContext.getAccountId();
        Optional<Account> optionalAccount = accountService.getAccountById(accountId);
        if (optionalAccount.isEmpty()) {
            throw new NotFoundException(ErrorsString.ACCOUNT_NOT_FOUND);
        }

        Account account = optionalAccount.get();
        List<GrowthStep> growthSteps = new ArrayList<>();
        Short level = 0;
        Map<Short, Map<SportCategory, BigDecimal>> minRefereePayPerCategoryPerLevel = new HashMap<>();
        Map<Short, Map<SportCategory, BigDecimal>> maxRefereePayPerCategoryPerLevel = new HashMap<>();

        switch (account.getRole()) {
            case COORDINATOR:
                Optional<Coordinator> coordinator = accountService.getCoordinatorByAccountId(accountId);
                if (coordinator.isEmpty()) {
                    throw new NotFoundException(ErrorsString.COORDINATOR_NOT_FOUND);
                }

                growthSteps = accountService.getGrowthStepsByType(GrowthStepType.COORDINATOR);
                level = coordinator.get().getLevel();
                break;
            case REFEREE:
                Optional<Referee> referee = accountService.getRefereeByAccountId(accountId);
                if (referee.isEmpty()) {
                    throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
                }
                growthSteps = accountService.getGrowthStepsByType(GrowthStepType.REFEREE);
                level = referee.get().getLevel();
                // I livelli sono da 1 a 4
                for (Short i = 1; i <= 4; i++) {
                    Map<SportCategory, BigDecimal> minRefereePayPerCategory = new HashMap<>();
                    Map<SportCategory, BigDecimal> maxRefereePayPerCategory = new HashMap<>();
                    for (SportCategory sportCategory : SportCategory.values()) {
                        List<Sport> sports = sportService.getSportsBySportCategory(sportCategory);
                        for (Sport sport : sports) {
                            List<RefereeRole> refereeRoles = sportService
                                    .getRefereeRolesBySportIdAndLevel(sport.getId(), i);
                            for (RefereeRole refereeRole : refereeRoles) {
                                if (!minRefereePayPerCategory.containsKey(sportCategory) || refereeRole.getPayPerMatch()
                                        .compareTo(minRefereePayPerCategory.get(sportCategory)) < 0) {
                                    minRefereePayPerCategory.put(sportCategory, refereeRole.getPayPerMatch());
                                }
                                if (!maxRefereePayPerCategory.containsKey(sportCategory) || refereeRole.getPayPerMatch()
                                        .compareTo(maxRefereePayPerCategory.get(sportCategory)) > 0) {
                                    maxRefereePayPerCategory.put(sportCategory, refereeRole.getPayPerMatch());
                                }
                            }
                        }
                    }
                    minRefereePayPerCategoryPerLevel.put(i, minRefereePayPerCategory);
                    maxRefereePayPerCategoryPerLevel.put(i, maxRefereePayPerCategory);
                }
                break;
            default:
                throw new ForbiddenException(ErrorsString.INVALID_ACCOUNT_ROLE);
        }
        return MyGrowthResponse.builder().growthSteps(growthSteps.stream().map(GrowthStepDTO::fromGrowthStep)
                .toList()).level(level).minRefereePayPerCategoryPerLevel(minRefereePayPerCategoryPerLevel)
                .maxRefereePayPerCategoryPerLevel(maxRefereePayPerCategoryPerLevel).build();
    }
}
