package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Entity
@Table(name = "field")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(name = "organization_id", nullable = false)
     * private Long organizationId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /*
     * @Column(name = "city_id", nullable = false)
     * private Long cityId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(length = 31)
    private String name;

    @Column(name = "location_lat", nullable = false)
    private Double locationLat;

    @Column(name = "location_lng", nullable = false)
    private Double locationLng;

    @Column(nullable = false, length = 63)
    private String address;

    @Column(name = "hour_cost_tournament", nullable = false)
    private BigDecimal hourCostTournament;

    @Column(name = "hour_cost_private", nullable = false)
    private BigDecimal hourCostPrivate;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "time_zone", nullable = false, length = 63)
    private String timeZone = "Europe/Rome";

    @Column(name = "closed_days", nullable = false, length = 7)
    private String closedDays = "0000001";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public String getCompleteAddress(){
        return address + ", " + city.getName();
    }

    public BigDecimal calculateTournamentCost(BigDecimal durationInHours) {
        return hourCostTournament.multiply(durationInHours);
    }

    public BigDecimal calculatePrivateCost(Instant startTime, Instant finishTime) {
        BigDecimal durationInHours = BigDecimal.valueOf(Duration.between(startTime, finishTime).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return hourCostPrivate.multiply(durationInHours);
    }

    public ZoneId getZoneId() {
        return ZoneId.of(timeZone);
    }
}
