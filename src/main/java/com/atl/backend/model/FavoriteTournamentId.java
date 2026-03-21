package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class FavoriteTournamentId implements Serializable {
    private Integer account;
    private Integer tournament;
}
