package com.atl.backend.repository;

import com.atl.backend.model.FriendInvite;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendInviteRepository extends JpaRepository<FriendInvite, Long> {
    Integer countByInviterAccount_IdAndRedeemedTrue(Long inviterAccountId);

    Integer countByInviterAccount_Id(Long inviterAccountId);

    Optional<FriendInvite> findByInvitedAccount_IdAndRedeemedFalse(Long invitedAccountId);
}
