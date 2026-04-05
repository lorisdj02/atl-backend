package com.atl.backend.model.enums;
public enum TournamentStatus {
    CREATED(1),
    SCHEDULED(2),
    FINISHED(3),
    PENDING_PAYMENTS(4),
    CANCELED(5);

    private final int priority;
    TournamentStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
