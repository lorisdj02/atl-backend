package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class TournamentParticipationId implements Serializable {
    private Integer tournamentId;
    private Integer teamId;
}