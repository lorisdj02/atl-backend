package com.atl.backend.response;

import java.util.HashMap;
import java.util.List;
import java.math.BigDecimal;
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
public class InviteFriendsResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.inviteBalance)
    private final BigDecimal inviteBalance;

    @NotNull
    @JsonProperty(ApiJsonKeys.referralCode)
    private final String referralCode;

    @NotNull
    @JsonProperty(ApiJsonKeys.numRedeemed)
    private final Integer numRedeemed;

    @NotNull
    @JsonProperty(ApiJsonKeys.numInvites)
    private final Integer numInvites;

    /*
     * final double inviteBalance;
     * final String referralCode;
     * final int numRedeemed;
     * final int numInvites;
     */
}
