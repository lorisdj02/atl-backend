package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "field")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "organization_id", nullable = false)
     * private Integer organizationId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /*
     * @Column(name = "city_id", nullable = false)
     * private Integer cityId;
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

    @Column(name = "closed_days", nullable = false, length = 7)
    private String closedDays = "0000001";

    public String getCompleteAddress(){
        return address + ", " + city.getName();
    }
}
