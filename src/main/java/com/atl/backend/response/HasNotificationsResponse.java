package com.atl.backend.response;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HasNotificationsResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.hasUnreadNotifications)
    private Boolean hasUnreadNotifications;

    @JsonProperty(ApiJsonKeys.isVip)
    private Boolean isVip;
}
