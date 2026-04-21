package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class FootballStandingId implements Serializable {
    private Long tournament;
    private Long team;
}