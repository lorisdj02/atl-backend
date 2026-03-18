package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "\"user\"")
public class User {
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

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 31)
    private String name;

    @Column(nullable = false, length = 31)
    private String surname;

    private Boolean vip = false;

    @Column(name = "vip_type", length = 15)
    private String vipType = "NONE";

    @Column(name = "vip_start")
    private LocalDate vipStart;

    @Column(name = "vip_end")
    private LocalDate vipEnd;

    @Column(nullable = false, length = 15)
    private String role;

    @Column(length = 15)
    private String status = "ACTIVE";

    @Column(name = "preferred_city_id")
    private Integer preferredCityId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}
