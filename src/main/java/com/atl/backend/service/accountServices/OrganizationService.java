package com.atl.backend.service.accountServices;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.config.ErrorsString;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Organization;
import com.atl.backend.repository.OrganizationRepository;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationRepository organizationRepository;

    public Organization registerOrganization(Account account, Optional<String> orgName) {
        if (orgName.isEmpty() || orgName.get().isBlank()) {
            throw new BadRequestException(ErrorsString.ORGANIZATION_NEED_ORGANIZATION_NAME);
        }
        Organization organization = new Organization();
        organization.setAccount(account);
        organization.setOrgName(orgName.get());
        return organizationRepository.save(organization);
    }

    public void deleteAllOrganizations() {
        organizationRepository.deleteAll();
    }

    public void deleteOrganizationByAccountId(Integer accountId) {
        organizationRepository.deleteByAccount_Id(accountId);
    }

    public Optional<Organization> getOrganizationByAccountId(Integer accountId) {
        return organizationRepository.findByAccount_Id(accountId);
    }

    public Optional<Organization> getOrganizationByOrganizationId(Integer orgId) {
        return organizationRepository.findByOrganizationId(orgId);
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }
}
