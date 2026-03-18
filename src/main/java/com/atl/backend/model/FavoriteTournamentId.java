package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class FavoriteTournamentId implements Serializable {
    private Integer accountId;
    private Integer tournamentId;
}
