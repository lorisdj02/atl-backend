package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "referee")
public class Referee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referee_id")
    private Integer refereeId;

    /*
     * @Column(name = "account_id", unique = true, nullable = false)
     * private Integer accountId;
     */

    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(nullable = false)
    private Short level = 1;
}
