package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "friend_invite")
public class FriendInvite {
    @Id
    private Integer invited;

    @Column(nullable = false)
    private Integer inviter;

    @Column(nullable = false)
    private Boolean redeemed = false;
}
