package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "final_stage_bracket", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "tournament_id", "group_name" })
})
public class FinalStageBracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "tournament_id", nullable = false)
     * private Integer tournamentId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "group_name", length = 3, nullable = false)
    private String groupName;

    @Column(name = "aggregate_score_home", nullable = false)
    private Short aggregateScoreHome = 0;

    @Column(name = "aggregate_score_away", nullable = false)
    private Short aggregateScoreAway = 0;

    /*
     * @Column(name = "next_final_stage_bracket_id")
     * private Integer nextFinalStageBracketId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_final_stage_bracket_id")
    private FinalStageBracket nextFinalStageBracket;

    @Enumerated(EnumType.STRING)
    @Column(name = "next_final_stage_bracket_slot", length = 15)
    private MatchSlotType nextFinalStageBracketSlot;
}
