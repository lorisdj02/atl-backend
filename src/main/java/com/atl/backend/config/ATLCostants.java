package com.atl.backend.config;

import java.math.BigDecimal;
import java.time.Instant;

public class ATLCostants {
    public static final BigDecimal ORGANIZATION_FEE_ATL = new BigDecimal(0.03);

    public static final BigDecimal USER_FEE_ATL = new BigDecimal(0.05);

    public static final BigDecimal TEAM_FEE_ATL = new BigDecimal(50);

    public static final BigDecimal INVITE_REWARD = new BigDecimal(2.5);

    public static Instant getRescheduleLimit() {
        return Instant.now().plusSeconds(24 * 3600); // 24 ore prima della prenotazione
    }

    public static Instant getBookingRefundLimit() {
        return Instant.now().plusSeconds(3600); // 1 ora prima della prenotazione
    }

    // Usare per scheduling automatico di cancellazione prenotazione se non viene riempita entro un certo limite di tempo
    public static Instant getLobbyBookingFillLimit() {
        return Instant.now().plusSeconds(2 * 3600); // 2 ore prima della prenotazione
    }

    // non implementato ancora, da valutare se è il caso di implementarlo o meno
    public static Instant getBookingRefundExtraordinaryLimit() {
        return Instant.now().plusSeconds(3 * 3600); // 3 ore prima della prenotazione
    }

}
