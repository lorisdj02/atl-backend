package com.atl.backend.service.accountServices;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.RefereeLicenseDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Referee;
import com.atl.backend.model.RefereeLicense;
import com.atl.backend.model.enums.LicenseStatus;
import com.atl.backend.repository.RefereeLicenseRepository;
import com.atl.backend.repository.RefereeRepository;
import com.atl.backend.service.SportService;

@Service
public class RefereeService {

    @Autowired
    private RefereeRepository refereeRepository;
    @Autowired
    private SportService sportService;
    @Autowired
    private RefereeLicenseRepository refereeLicenseRepository;


    public Referee registerReferee(Account account) {
        Referee referee = new Referee();
        referee.setAccount(account);
        referee.setLevel((short) 0);
        return refereeRepository.save(referee);
    }

    public Short getRefereeLevelByAccountId(Long accountId) {
        Referee referee = refereeRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.REFEREE_NOT_FOUND));
        return referee.getLevel();
    }

    public void deleteRefereeByAccountId(Long accountId) {
        Referee referee = refereeRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.REFEREE_NOT_FOUND));
        refereeRepository.delete(referee);
    }

    public void deleteAllReferees() {
        refereeRepository.deleteAll();
    }

    public Optional<Referee> getRefereeByAccountId(Long accountId) {
        return refereeRepository.findByAccount_Id(accountId);
    }

    public Optional<Referee> getRefereeByRefereeId(Long refereeId) {
        return refereeRepository.findById(refereeId);
    }

    public List<Referee> getAllReferees() {
        return refereeRepository.findAll();
    }

    public Short getMedianRefereeLevel() {
        List<Short> levels = refereeRepository.findAllLevelsOrdered();
        if (levels == null || levels.isEmpty()) {
            throw new NotFoundException(ErrorsString.REFEREE_NOT_FOUND);
        }
        int n = levels.size();
        int index = (n - 1) / 2; // odd -> middle; even -> lower middle
        return levels.get(index);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefereeLicense insertRefereeLicense(RefereeLicenseDTO refereeLicenseDTO) {
        Referee referee = AccountContext.getActiveReferee();
        if (!referee.getRefereeId().equals(refereeLicenseDTO.getRefereeId())) {
            throw new NotFoundException(ErrorsString.REFEREE_LICENSE_MISMATCH);
        }

        RefereeLicense refereeLicense = new RefereeLicense();
        refereeLicense.setReferee(referee);
        refereeLicense.setSport(
                sportService.getSportById(refereeLicenseDTO.getSportId())
                        .orElseThrow(() -> new NotFoundException(ErrorsString.SPORT_NOT_FOUND)));
        refereeLicense.setPhotoUrl(refereeLicenseDTO.getPhotoUrl());
        // TODO: Imposta a ACTIVE solo dopo la verifica da parte di collaboratore,
        // adesso per debug
        refereeLicense.setStatus(LicenseStatus.ACTIVE);
        refereeLicense.setCreatedAt(LocalDateTime.now());
        try{
            LocalDate issuedAt = LocalDate.parse(refereeLicenseDTO.getIssuedAt());
            refereeLicense.setIssuedAt(issuedAt);
        } catch (Exception e) {
            throw new BadRequestException(ErrorsString.INVALID_TIME_RANGE);
        }
        return refereeLicenseRepository.save(refereeLicense);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRefereeLicense(Long sportId) {
        Referee referee = AccountContext.getActiveReferee();
        RefereeLicense refereeLicense = refereeLicenseRepository
                .findByReferee_RefereeIdAndSport_Id(referee.getRefereeId(), sportId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.REFEREE_LICENSE_NOT_FOUND));
        refereeLicenseRepository.delete(refereeLicense);
    }
}
