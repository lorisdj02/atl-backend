package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friend_invite")
public class FriendInvite {
    @Id
    @Column(name = "invited_account_id")
    private Integer invitedAccountId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_account_id", insertable = false, updatable = false)
    private Account invitedAccount;

    /*
     * @Column(name = "inviter_account_id", nullable = false)
     * private Integer inviterAccountId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_account_id", nullable = false)
    private Account inviterAccount;

    @Column(name = "redeemed", nullable = false)
    private Boolean redeemed = false;
}
