package com.atl.backend.model;

import java.time.Instant;
import java.time.ZoneId;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "city")
public class City {
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "time_zone", nullable = false, length = 63)
    private String timeZone = "Europe/Rome";

    public ZoneId getZoneId() {
        return ZoneId.of(timeZone);
    }
}
