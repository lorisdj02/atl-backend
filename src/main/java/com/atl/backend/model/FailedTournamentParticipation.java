package com.atl.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "failed_tournament_participation", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "team_id"})
})
public class FailedTournamentParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "tournament_id")
     * private Integer tournamentId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    /*
     * @Column(name = "team_id")
     * private Integer teamId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    static public FailedTournamentParticipation fromTournamentAndTeam(Tournament tournament, Team team) {
        FailedTournamentParticipation failedParticipation = new FailedTournamentParticipation();
        failedParticipation.setTournament(tournament);
        failedParticipation.setTeam(team);
        return failedParticipation;
    }
}
