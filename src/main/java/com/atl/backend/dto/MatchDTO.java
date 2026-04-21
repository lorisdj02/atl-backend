package com.atl.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.atl.backend.model.Match;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchDTO {

    @NotNull
    @JsonProperty("id")
    private Long id;

    @JsonProperty("scheduledStart")
    private LocalDateTime scheduledStart;

    @JsonProperty("startMatch")
    private LocalDateTime startMatch;

    @JsonProperty("finishMatch")
    private LocalDateTime finishMatch;

    @NotNull
    @JsonProperty("tournamentId")
    private Long tournamentId;

    @JsonProperty("motmPlayerId")
    private Long motmPlayerId;

    @JsonProperty("groupName")
    private String groupName;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    @JsonProperty("legOrder")
    private String legOrder;

    @JsonProperty("coordinatorStatus")
    @NotNull
    private String coordinatorStatus;

    @JsonProperty("finalStageBracketId")
    private Long finalStageBracketId;

    public static MatchDTO fromMatch(Match match) {
        if (match == null) {
            return null;
        }
        MatchDTO dto = new MatchDTO();
        dto.setId(match.getId());
        dto.setScheduledStart(match.getScheduledStart());
        dto.setStartMatch(match.getStartMatch() != null ? match.getStartMatch() : null);
        dto.setFinishMatch(match.getFinishMatch() != null ? match.getFinishMatch() : null);
        dto.setTournamentId(match.getTournament().getId());
        dto.setMotmPlayerId(match.getMotmPlayer() != null ? match.getMotmPlayer().getPlayerId() : null);
        dto.setGroupName(match.getGroupName());
        dto.setSportCategory(match.getSportCategory().toString());
        dto.setLegOrder(match.getLegOrder() != null ? match.getLegOrder().toString() : null);
        dto.setCoordinatorStatus(match.getCoordinatorStatus() != null ? match.getCoordinatorStatus().toString() : null);
        dto.setFinalStageBracketId(
                match.getFinalStageBracket() != null ? match.getFinalStageBracket().getId() : null);

        return dto;
    }
}
