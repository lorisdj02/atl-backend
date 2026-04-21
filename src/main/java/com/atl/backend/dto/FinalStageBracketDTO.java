package com.atl.backend.dto;

import com.atl.backend.model.FinalStageBracket;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinalStageBracketDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("tournamentId")
    private Long tournamentId;

    @NotNull
    @JsonProperty("groupName")
    private String groupName;

    @NotNull
    @JsonProperty("aggregateScoreHome")
    private Short aggregateScoreHome = 0;

    @NotNull
    @JsonProperty("aggregateScoreAway")
    private Short aggregateScoreAway = 0;

    @JsonProperty("nextFinalStageBracketId")
    private Long nextFinalStageBracketId;

    @JsonProperty
    private String nextFinalStageBracketSlot;

    public static FinalStageBracketDTO fromFinalStageBracket(FinalStageBracket finalStageBracket) {
        if (finalStageBracket == null) {
            return null;
        }

        FinalStageBracketDTO dto = new FinalStageBracketDTO();
        dto.setId(finalStageBracket.getId());
        dto.setTournamentId(finalStageBracket.getTournament().getId());
        dto.setGroupName(finalStageBracket.getGroupName());
        dto.setAggregateScoreHome(finalStageBracket.getAggregateScoreHome());
        dto.setAggregateScoreAway(finalStageBracket.getAggregateScoreAway());
        if (finalStageBracket.getNextFinalStageBracket() != null) {
            dto.setNextFinalStageBracketId(finalStageBracket.getNextFinalStageBracket().getId());
            dto.setNextFinalStageBracketSlot(finalStageBracket.getNextFinalStageBracketSlot().toString());
        }
        return dto;
    }
}
