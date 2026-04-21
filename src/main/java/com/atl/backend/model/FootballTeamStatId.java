package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class FootballTeamStatId implements Serializable {
    private Long playerId;
    private Long teamId;
}