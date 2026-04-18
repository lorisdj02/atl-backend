package com.atl.backend.repository;

import com.atl.backend.model.FriendInvite;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendInviteRepository extends JpaRepository<FriendInvite, Integer> {
    Integer countByInviterAccount_IdAndRedeemedTrue(Integer inviterAccountId);

    Integer countByInviterAccount_Id(Integer inviterAccountId);

    Optional<FriendInvite> findByInvitedAccount_IdAndRedeemedFalse(Integer invitedAccountId);
}
