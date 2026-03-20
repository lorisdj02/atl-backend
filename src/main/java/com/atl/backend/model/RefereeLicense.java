package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@IdClass(RefereeLicenseId.class)
@Table(name = "referee_license")
public class RefereeLicense {
    @Id
    @Column(name = "sport_id", nullable = false)
    private Integer sportId;

    @MapsId("sportId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false, insertable = false, updatable = false)
    private Sport sport;

    @Id
    @Column(name = "referee_id", nullable = false)
    private Integer refereeId;

    @MapsId("refereeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id", nullable = false, insertable = false, updatable = false)
    private Referee referee;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 31)
    private LicenseStatus status = LicenseStatus.NOT_VERIFIED;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
