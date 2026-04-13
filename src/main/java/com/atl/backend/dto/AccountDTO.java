package com.atl.backend.dto;


import com.atl.backend.model.Account;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    @NotNull
    @JsonProperty("accountId")
    private Integer accountId;

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

    @NotNull
    @JsonProperty("imageUrl")
    private String imageUrl;

    @NotNull
    @JsonProperty("role")
    private String role;

    @NotNull
    @JsonProperty("birthdate")
    private String birthdate;

    public static AccountDTO fromAccount(Account account) {
        if (account == null) {
            return null;
        }
        AccountDTO dto = new AccountDTO();
        dto.setAccountId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setName(account.getName());
        dto.setSurname(account.getSurname());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setImageUrl(account.getImageUrl());
        dto.setRole(account.getRole().toString());
        dto.setBirthdate(account.getBirthdate() != null ? account.getBirthdate().toString() : null);
        return dto;
    }
}
