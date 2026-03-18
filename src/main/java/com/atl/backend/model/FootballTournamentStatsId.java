package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class FootballTournamentStatsId implements Serializable {
    private Integer playerId;
    private Integer tournamentId;
}