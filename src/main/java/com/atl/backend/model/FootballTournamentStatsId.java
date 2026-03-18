package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class FootballTournamentStatsId implements Serializable {
    private Integer playerId;
    private Integer tournamentId;
}