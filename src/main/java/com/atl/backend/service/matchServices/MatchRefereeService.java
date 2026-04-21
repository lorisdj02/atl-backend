package com.atl.backend.service.matchServices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.ConflictException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.Match;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeAssignment;
import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.FootballMatchRepository;
import com.atl.backend.repository.MatchRepository;
import com.atl.backend.repository.RefereeAssignmentRepository;
import com.atl.backend.repository.RefereeRoleRepository;

@Service
public class MatchRefereeService {
    @Autowired
    private RefereeAssignmentRepository refereeAssignmentRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private RefereeRoleRepository refereeRoleRepository;
    @Autowired
    private FootballMatchRepository footballMatchRepository;

    public List<RefereeAssignment> getRefereeAssignmentsByRefereeId(Long refereeId) {
        return refereeAssignmentRepository.findByReferee_RefereeId(refereeId);
    }

    @Transactional(readOnly = true)
    public Boolean hasRefereeActiveMatchesByRefereeId(Long refereeId) {
        List<RefereeAssignment> refereeAssignments = refereeAssignmentRepository.findByReferee_RefereeId(refereeId);
        for (RefereeAssignment refereeAssignment : refereeAssignments) {
            Match match = refereeAssignment.getMatch();
            if (match.getFinishMatch() == null) {
                return true;
            }
        }
        return false;
    }

    public List<RefereeAssignment> getRefereeAssignmentsForMatch(Long matchId) {
        return refereeAssignmentRepository.findByMatch_Id(matchId);
    }

    public BigDecimal getRefereeRolePayPerMatch(Long sportId, String name, Short level) {
        List<RefereeRole> refereeRoles = refereeRoleRepository.findBySport_IdAndName(sportId, name);
        if (refereeRoles.isEmpty()) {
            throw new NotFoundException(ErrorsString.REFEREE_ROLE_NOT_FOUND);
        }
        refereeRoles.sort((r1, r2) -> r1.getLevel().compareTo(r2.getLevel()));
        BigDecimal maxPayPerMatch = BigDecimal.ZERO;
        for (RefereeRole refereeRole : refereeRoles) {
            if (refereeRole.getLevel() <= level && refereeRole.getPayPerMatch().compareTo(maxPayPerMatch) > 0) {
                maxPayPerMatch = refereeRole.getPayPerMatch();
            } else {
                break;
            }
        }

        return maxPayPerMatch;
    }

    public List<Long> getRefereeIdsByMatchId(Long matchId) {
        List<RefereeAssignment> refereeAssignments = refereeAssignmentRepository.findByMatch_Id(matchId);
        List<Long> refereeIds = new ArrayList<>();
        for (RefereeAssignment refereeAssignment : refereeAssignments) {
            refereeIds.add(refereeAssignment.getReferee().getRefereeId());
        }
        return refereeIds;
    }

    public void insertRefereeAssignment(Long matchId, String refereeRoleName) {
        Referee referee = AccountContext.getActiveReferee();
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.MATCH_NOT_FOUND));
        if (match.getScheduledStart() == null) {
            throw new ConflictException(ErrorsString.MATCH_NOT_SCHEDULED);
        }
        List<RefereeAssignment> refereeAssignments = refereeAssignmentRepository.findByMatch_Id(matchId);
        if (refereeAssignments.size() >= match.getTournament().getSport().getNumRefereesPerMatch()) {
            throw new ConflictException(ErrorsString.MATCH_ALREADY_HAS_ENOUGH_REFEREES);
        }

        BigDecimal payPerMatch = getRefereeRolePayPerMatch(match.getTournament().getSport().getId(),
                refereeRoleName, referee.getLevel());

        RefereeAssignment refereeAssignment = new RefereeAssignment();
        refereeAssignment.setMatch(match);
        refereeAssignment.setReferee(referee);
        refereeAssignment.setRole(refereeRoleName);
        refereeAssignment.setPay(payPerMatch);
        refereeAssignmentRepository.save(refereeAssignment);
    }

    public List<FootballMatch> getAssignedFootballMatchesByRefereeId(Long refereeId) {
        List<RefereeAssignment> refereeAssignments = getRefereeAssignmentsByRefereeId(refereeId);
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
}
