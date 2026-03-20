package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "field_id", nullable = false)
     * private Integer fieldId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Column(name = "cancel_description", length = 255)
    private String cancelDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private BookingStatus status = BookingStatus.BOOKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "finish_time", nullable = false)
    private LocalDateTime finishTime;
}
