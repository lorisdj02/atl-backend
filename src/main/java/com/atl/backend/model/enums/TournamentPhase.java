package com.atl.backend.model.enums;
public enum TournamentPhase {
    GROUP (1),
    RO32 (2),
    RO16 (3),
    QUARTER (4),
    SEMI (5),
    FINAL (6);

    private final int order;
    TournamentPhase(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
