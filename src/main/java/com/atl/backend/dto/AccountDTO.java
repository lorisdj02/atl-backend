package com.atl.backend.dto;

import com.atl.backend.model.Account;
import com.atl.backend.util.PublicUrlUtil;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

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

    @NotNull
    @JsonProperty("phone")
    private String phone;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @NotNull
    @JsonProperty("role")
    private String role;

    @NotNull
    @JsonProperty("birthdate")
    private String birthdate;

    @JsonProperty("isVip")
    private Boolean isVip;

    @JsonProperty("vipType")
    private String vipType;

    @JsonProperty("vipStart")
    private String vipStart;

    @JsonProperty("vipEnd")
    private String vipEnd;

    @JsonProperty("preferredCityId")
    private Long preferredCityId;

    @JsonProperty("createdAt")
    private String createdAt;

    public static AccountDTO fromAccount(Account account) {
        if (account == null) {
            return null;
        }
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setName(account.getName());
        dto.setSurname(account.getSurname());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setImageUrl(PublicUrlUtil.toPublicUrl(account.getImageUrl()));
        dto.setRole(account.getRole().toString());
        dto.setBirthdate(account.getBirthdate() != null ? account.getBirthdate().toString() : null);
        dto.setIsVip(account.getIsVip());
        dto.setVipType(account.getVipType() != null ? account.getVipType().toString() : null);
        dto.setVipStart(account.getVipStart() != null ? account.getVipStart().toString() : null);
        dto.setVipEnd(account.getVipEnd() != null ? account.getVipEnd().toString() : null);
        dto.setPreferredCityId(account.getPreferredCity() != null ? account.getPreferredCity().getId() : null);
        dto.setCreatedAt(account.getCreatedAt().toString());
        return dto;
    }
}
