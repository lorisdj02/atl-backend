package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class FootballTournamentStatsId implements Serializable {
    private Long player;
    private Long tournament;
}