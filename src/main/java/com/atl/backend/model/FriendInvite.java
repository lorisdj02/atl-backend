package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friend_invite")
public class FriendInvite {
    @Id
    @Column(name = "invited_account_id")
    private Integer invitedAccountId;

    @Column(name = "inviter_account_id", nullable = false)
    private Integer inviterAccountId;

    @Column(name = "redeemed", nullable = false)
    private Boolean redeemed = false;
}
