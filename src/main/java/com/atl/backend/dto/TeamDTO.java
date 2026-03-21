package com.atl.backend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamDTO {

    @NotNull
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("cityId")
    private Integer cityId;

    @NotNull
    @JsonProperty("captainPlayerId")
    private Integer captainPlayerId;

    @JsonProperty("vice1PlayerId")
    private Integer vice1PlayerId;

    @JsonProperty("vice2PlayerId")
    private Integer vice2PlayerId;

    @JsonProperty("logoUrl")
    private String logoUrl;

    @NotNull
    @JsonProperty("rankPts")
    private Integer rankPts = 0;

    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("alias")
    private String alias;

    @JsonProperty("description")
    @Size(max = 511)
    private String description;

    @JsonProperty("vip")
    private Boolean vip = false;

    @JsonProperty("maxPlayers")
    private Integer maxPlayers;

    @JsonProperty("players")
    private Integer players;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    public static TeamDTO fromTeam(com.atl.backend.model.Team team) {
        if (team == null) {
            return null;
        }

        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setCityId(team.getCity().getId());
        dto.setCaptainPlayerId(team.getCaptainPlayer().getPlayerId());
        if (team.getVice1Player() != null) {
            dto.setVice1PlayerId(team.getVice1Player().getPlayerId());
        }
        if (team.getVice2Player() != null) {
            dto.setVice2PlayerId(team.getVice2Player().getPlayerId());
        }
        dto.setLogoUrl(team.getLogoUrl());
        dto.setRankPts(team.getRankPts());
        dto.setName(team.getName());
        dto.setAlias(team.getAlias());
        dto.setDescription(team.getDescription());
        dto.setVip(team.getVip());
        dto.setMaxPlayers(team.getMaxPlayers());
        dto.setPlayers(team.getPlayers());
        dto.setSportCategory(team.getSportCategory().toString());
        return dto;
    }
}