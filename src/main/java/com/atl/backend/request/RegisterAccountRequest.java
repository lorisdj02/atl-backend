package com.atl.backend.request;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterAccountRequest {
    @NotNull
    @NotBlank
    @Email
    @JsonProperty(ApiJsonKeys.email)
    private String email;

    @NotNull
    @NotBlank
    @JsonProperty(ApiJsonKeys.username)
    private String username;

    @NotNull
    @NotBlank
    @JsonProperty(ApiJsonKeys.fireBaseUid)
    private String fireBaseUid;

    @NotNull
    @NotBlank
    @JsonProperty(ApiJsonKeys.name)
    private String name;

    @NotNull
    @NotBlank
    @JsonProperty(ApiJsonKeys.surname)
    private String surname;

    @NotNull
    @NotBlank
    @JsonProperty(ApiJsonKeys.role)
    private String role;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
    @JsonProperty(ApiJsonKeys.phone)
    private String phone;

    @NotNull
    @NotBlank
    @JsonProperty(ApiJsonKeys.birthdate)
    private String birthdate;

    @JsonProperty(ApiJsonKeys.inviteCode)
    private String inviteCode;

    @JsonProperty(ApiJsonKeys.organizationName)
    private String organizationName;

    @JsonProperty(ApiJsonKeys.profilePictureUrl)
    private String profilePictureUrl;

}
