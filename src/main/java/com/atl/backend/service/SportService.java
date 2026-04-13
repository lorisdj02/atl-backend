package com.atl.backend.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.RefereeLicense;
import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.Sport;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.RefereeLicenseRepository;
import com.atl.backend.repository.RefereeRoleRepository;
import com.atl.backend.repository.SportRepository;

@Service
public class SportService {
    @Autowired
    private SportRepository sportRepository;
    @Autowired
    private RefereeLicenseRepository refereeLicenseRepository;
    @Autowired
    private RefereeRoleRepository refereeRoleRepository;

    public Boolean isSupportedSportCategory(String sportCategory) {
        try {
            SportCategory.valueOf(sportCategory.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public SportService(SportRepository sportRepository, RefereeLicenseRepository refereeLicenseRepository,
            RefereeRoleRepository refereeRoleRepository) {
        this.sportRepository = sportRepository;
        this.refereeLicenseRepository = refereeLicenseRepository;
        this.refereeRoleRepository = refereeRoleRepository;
    }

    public void saveSport(com.atl.backend.model.Sport sport) {
        sportRepository.save(sport);
    }

    public Optional<Sport> getSportById(Integer id) {
        return sportRepository.findById(id);
    }

    public Optional<Sport> getSportByName(String name) {
        return sportRepository.findByName(name);
    }

    public List<Sport> getAllSports() {
        return sportRepository.findAll();
    }

    public void deleteAllSports() {
        sportRepository.deleteAll();
    }

    public void saveRefereeRole(RefereeRole refereeRole) {
        refereeRoleRepository.save(refereeRole);
    }

    public void deleteAllRefereeRoles() {
        refereeRoleRepository.deleteAll();
    }

    public List<RefereeRole> getAllRefereeRoles() {
        return refereeRoleRepository.findAll();
    }

    public List<Sport> getSportsBySportCategory(SportCategory sportCategory) {
        return sportRepository.findBySportCategory(sportCategory);
    }

    public List<RefereeRole> getRefereeRolesBySportId(Integer sportId) {
        return refereeRoleRepository.findBySport_Id(sportId);
    }

    public List<RefereeRole> getRefereeRolesBySportIdAndLevel(Integer sportId, Short level) {
        return refereeRoleRepository.findBySport_IdAndLevel(sportId, level);
    }

    public List<RefereeLicense> getRefereeLicensesByRefereeId(Integer refereeId) {
        return refereeLicenseRepository.findByReferee_RefereeId(refereeId);
    }

    public Optional<RefereeLicense> getRefereeLicensesByRefereeIdAndSportId(Integer refereeId, Integer sportId) {
        return refereeLicenseRepository.findByReferee_RefereeIdAndSport_Id(refereeId, sportId);
    }

    public Map<String, BigDecimal> getRefereePayPerMatchBySportIdAndLevel(Integer sportId, Short level) {
        Optional<Sport> sportOpt = sportRepository.findById(sportId);
        if (sportOpt.isEmpty()) {
            throw new NotFoundException(ErrorsString.SPORT_NOT_FOUND);
        }
        List<RefereeRole> roles = refereeRoleRepository.findBySport_IdAndLevel(sportId, level);
        if (roles == null || roles.isEmpty()) {
            throw new NotFoundException(ErrorsString.REFEREE_ROLE_NOT_FOUND);
        }
        Map<String, BigDecimal> pay = new HashMap<>();
        for (RefereeRole role : roles) {
            pay.put(role.getName(), role.getPayPerMatch());
        }
        return pay;

    }

    public List<Sport> getSportsByCategory(SportCategory sportCategory) {
        return sportRepository.findBySportCategory(sportCategory);
    }

    public void checkSportExists(Integer sportId) {
        if (!sportRepository.existsById(sportId)) {
            throw new NotFoundException(ErrorsString.SPORT_NOT_FOUND);
        }
    }
}
