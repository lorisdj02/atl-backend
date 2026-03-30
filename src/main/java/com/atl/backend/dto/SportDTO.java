package com.atl.backend.dto;

import com.atl.backend.model.Sport;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SportDTO {
    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    @NotNull
    @JsonProperty("minPlayersPerTeam")
    private Short minPlayersPerTeam;

    @NotNull
    @JsonProperty("maxPlayersPerTeam")
    private Short maxPlayersPerTeam;

    @NotNull
    @JsonProperty("minutesPerRound")
    private Short minutesPerRound;

    @NotNull
    @JsonProperty("minutesPerPause")
    private Short minutesPerPause;

    @NotNull
    @JsonProperty("minReferee")
    private Short minReferee;

    public static SportDTO fromSport(Sport sport) {
        if (sport == null) {
            return null;
        }

        SportDTO dto = new SportDTO();
        dto.setName(sport.getName());
        dto.setSportCategory(sport.getSportCategory().name());
        dto.setMinPlayersPerTeam(sport.getMinPlayersPerTeam());
        dto.setMaxPlayersPerTeam(sport.getMaxPlayersPerTeam());
        dto.setMinutesPerRound(sport.getMinutesPerRound());
        dto.setMinutesPerPause(sport.getMinutesPerPause());
        dto.setMinReferee(sport.getMinReferee());
        return dto;
    }
}
