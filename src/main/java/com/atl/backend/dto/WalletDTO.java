package com.atl.backend.dto;

import java.math.BigDecimal;

import com.atl.backend.model.Wallet;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletDTO {
    @NotNull
    @JsonProperty("accountId")
    private Integer accountId;

    @NotNull
    @JsonProperty("prizeBalance")
    private BigDecimal prizeBalance;

    @NotNull
    @JsonProperty("personalBalance")
    private BigDecimal personalBalance;

    @NotNull
    @JsonProperty("inviteBalance")
    private BigDecimal inviteBalance;

    @NotNull
    @JsonProperty("prizeLastWithdraw")
    private String prizeLastWithdraw;

    @NotNull
    @JsonProperty("personalLastWithdraw")
    private String personalLastWithdraw;

    static public WalletDTO fromWallet(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setAccountId(wallet.getAccount().getId());
        dto.setPrizeBalance(wallet.getPrizeBalance());
        dto.setPersonalBalance(wallet.getPersonalBalance());
        dto.setInviteBalance(wallet.getInviteBalance());
        dto.setPrizeLastWithdraw(wallet.getPrizeLastWithdraw() != null ? wallet.getPrizeLastWithdraw().toString() : null);
        dto.setPersonalLastWithdraw(wallet.getPersonalLastWithdraw() != null ? wallet.getPersonalLastWithdraw().toString() : null);
        return dto;
    }
}
