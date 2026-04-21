package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class FavoriteTournamentId implements Serializable {
    private Long account;
    private Long tournament;
}
