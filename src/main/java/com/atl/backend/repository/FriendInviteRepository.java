package com.atl.backend.repository;

import com.atl.backend.model.FriendInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendInviteRepository extends JpaRepository<FriendInvite, Integer> {
}
