package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class TournamentParticipationId implements Serializable {
    private Integer tournament;
    private Integer team;
}