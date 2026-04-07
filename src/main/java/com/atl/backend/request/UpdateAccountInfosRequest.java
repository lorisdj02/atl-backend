package com.atl.backend.request;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class UpdateAccountInfosRequest {
    
    @JsonProperty(ApiJsonKeys.username)
    private String username;

    @JsonProperty(ApiJsonKeys.phone)
    private String phone;

    @JsonProperty(ApiJsonKeys.birthdate)
    private String birthdate;

    @JsonProperty(ApiJsonKeys.imageUrl)
    private String imageUrl;

    @JsonProperty(ApiJsonKeys.preferredCityId)
    private Integer preferredCityId;
}
