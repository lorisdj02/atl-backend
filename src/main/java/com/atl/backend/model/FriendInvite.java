package com.atl.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friend_invite")
public class FriendInvite {
    @Id
    private Integer invitedAccountId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_account_id")
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_transaction_id", nullable = false)
    private Transaction inviterTransaction;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
