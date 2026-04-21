package com.atl.backend.dto;

import com.atl.backend.model.Coordinator;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoordinatorDTO {
    @NotNull
    @JsonProperty("coordinatorId")
    private Long coordinatorId;

    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("surname")
    private String surname;

    @NotNull
    @JsonProperty("accountId")
    private Long accountId;

    @NotNull
    @JsonProperty("username")
    private String username;

    @JsonProperty("imageUrl")
    private String imageUrl;

    public static CoordinatorDTO fromCoordinator(Coordinator coordinator) {
        if (coordinator == null) {
            return null;
        }

        CoordinatorDTO dto = new CoordinatorDTO();
        dto.setCoordinatorId(coordinator.getCoordinatorId());
        dto.setName(coordinator.getAccount().getName());
        dto.setSurname(coordinator.getAccount().getSurname());
        dto.setAccountId(coordinator.getAccount().getId());
        dto.setUsername(coordinator.getAccount().getUsername());
        dto.setImageUrl(coordinator.getAccount().getImageUrl());
        return dto;
    }
}
