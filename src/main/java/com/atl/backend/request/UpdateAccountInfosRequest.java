package com.atl.backend.request;

import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
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
    private Long preferredCityId;
}
