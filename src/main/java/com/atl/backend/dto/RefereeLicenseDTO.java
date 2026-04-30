package com.atl.backend.dto;

import com.atl.backend.model.RefereeLicense;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefereeLicenseDTO {
    @NotNull
    @JsonProperty("sportId")
    private Long sportId;

    @NotNull
    @JsonProperty("refereeId")
    private Long refereeId;

    @JsonProperty("photoUrl")
    private String photoUrl;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("createdAt")
    private String createdAt;

    @NotNull
    @JsonProperty("issuedAt")
    private String issuedAt;

    @JsonProperty("expiresAt")
    private String expiresAt;

    @JsonProperty("verifiedAt")
    private String verifiedAt;

    @NotNull
    @JsonProperty("id")
    private String licenseId;

    public static RefereeLicenseDTO fromRefereeLicense(RefereeLicense refereeLicense) {
        if (refereeLicense == null) {
            return null;
        }
        RefereeLicenseDTO dto = new RefereeLicenseDTO();
        dto.setSportId(refereeLicense.getSport().getId());
        dto.setRefereeId(refereeLicense.getReferee().getRefereeId());
        dto.setPhotoUrl(refereeLicense.getPhotoUrl());
        dto.setStatus(refereeLicense.getStatus().toString());
        dto.setCreatedAt(refereeLicense.getCreatedAt().toString());
        dto.setIssuedAt(refereeLicense.getIssuedAt().toString());
        dto.setExpiresAt(refereeLicense.getExpiresAt() != null ? refereeLicense.getExpiresAt().toString() : null);
        dto.setVerifiedAt(refereeLicense.getVerifiedAt() != null ? refereeLicense.getVerifiedAt().toString() : null);
        dto.setLicenseId(refereeLicense.getLicenseId());
        return dto;
    }

}
