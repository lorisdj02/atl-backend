package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @Column(name = "stripe_id", unique = true)
    private String stripeId;

    @Column(unique = true, nullable = false, length = 15)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone", unique = true, nullable = false, length = 15)
    private String phone;

    @Column(name = "name", nullable = false, length = 31)
    private String name;

    @Column(name = "surname", nullable = false, length = 31)
    private String surname;

    @Column(name = "vip", nullable = false)
    private Boolean vip = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "vip_type", length = 15)
    private VipType vipType = VipType.NONE;

    @Column(name = "vip_start")
    private LocalDate vipStart;

    @Column(name = "vip_end")
    private LocalDate vipEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 15)
    private AccountRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private AccountStatus status = AccountStatus.ACTIVE;

    /*
     * @Column(name = "preferred_city_id")
     * private Integer preferredCityId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_city_id")
    private City preferredCity;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

        @Column(name = "image_url", length = 511)
    private String imageUrl;


    @Column(name = "favorite_sport_category", length = 15)
    @Enumerated(EnumType.STRING)
    private SportCategory favoriteSportCategory = SportCategory.FOOTBALL;
}
