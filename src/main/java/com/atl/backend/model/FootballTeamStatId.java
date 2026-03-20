package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class FootballTeamStatId implements Serializable {
    private Integer playerId;
    private Integer teamId;
}