package com.atl.backend.response;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.AccountDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    @JsonProperty(ApiJsonKeys.account)
    private AccountDTO account;
    /*
     * @JsonProperty(ApiJsonKeys.token)
     * private String token;
     */
    @JsonProperty(ApiJsonKeys.roleId)
    private Long roleId;

    @JsonProperty(ApiJsonKeys.teamId)
    private Long teamId;
}
