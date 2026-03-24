package com.atl.backend.dto;

import com.atl.backend.model.Player;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDTO {
    @NotNull
    @JsonProperty("playerId")
    private Integer playerId;

    @NotNull
    @JsonProperty("accountId")
    private Integer accountId;

    @NotNull
    @JsonProperty("username")
    private String username;

    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("surname")
    private String surname;

    @NotNull
    @JsonProperty("email")
    private String email;

    public static PlayerDTO fromPlayer(Player player) {
        if (player == null) {
            return null;
        }

        PlayerDTO dto = new PlayerDTO();
        dto.setPlayerId(player.getPlayerId());
        dto.setAccountId(player.getAccount().getId());
        dto.setUsername(player.getAccount().getUsername());
        dto.setName(player.getAccount().getName());
        dto.setSurname(player.getAccount().getSurname());
        dto.setEmail(player.getAccount().getEmail());
        return dto;
    }

}
