package com.atl.backend.response;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
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
    private Integer roleId;

    @JsonProperty(ApiJsonKeys.teamId)
    private Integer teamId;
}
