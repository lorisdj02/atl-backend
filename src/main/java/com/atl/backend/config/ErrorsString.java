package com.atl.backend.config;

public class ErrorsString {

    // Not found errors
    static final public String TEAM_NOT_FOUND = "TEAM_NOT_FOUND";

    static final public String PLAYER_NOT_FOUND = "PLAYER_NOT_FOUND";

    static final public String TOURNAMENT_NOT_FOUND = "TOURNAMENT_NOT_FOUND";

    static final public String FIELD_NOT_FOUND = "FIELD_NOT_FOUND";

    static final public String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";

    static final public String COORDINATOR_NOT_FOUND = "COORDINATOR_NOT_FOUND";

    static final public String REFEREE_NOT_FOUND = "REFEREE_NOT_FOUND";

    static final public String MATCH_NOT_FOUND = "MATCH_NOT_FOUND";

    static final public String BOOKING_NOT_FOUND = "BOOKING_NOT_FOUND";

    static final public String ORGANIZATION_NOT_FOUND = "ORGANIZATION_NOT_FOUND";

    static final public String WALLET_NOT_FOUND = "WALLET_NOT_FOUND";

    static final public String CHECKOUT_SESSION_NOT_FOUND = "CHECKOUT_SESSION_NOT_FOUND";

    static final public String SPORT_NOT_FOUND = "SPORT_NOT_FOUND";

    static final public String REFEREE_ROLE_NOT_FOUND = "REFEREE_ROLE_NOT_FOUND";

    static final public String CITY_NOT_FOUND = "CITY_NOT_FOUND";

    static final public String TRANSACTION_GROUP_NOT_FOUND = "TRANSACTION_GROUP_NOT_FOUND";

    static final public String TRANSACTION_NOT_FOUND = "TRANSACTION_NOT_FOUND";

    static final public String PURCHASE_NOT_FOUND = "PURCHASE_NOT_FOUND";

    static final public String TOURNAMENT_PARTICIPATION_NOT_FOUND = "TOURNAMENT_PARTICIPATION_NOT_FOUND";

    // Bad request errors

    static final public String UNSUPPORTED_SPORT_CATEGORY = "UNSUPPORTED_SPORT_CATEGORY";

    static final public String UNSUPPORTED_BOOKING_TYPE = "UNSUPPORTED_BOOKING_TYPE";

    static final public String INVALID_ACCOUNT_ROLE = "INVALID_ACCOUNT_ROLE";

    static final public String INVALID_TIME_RANGE = "INVALID_TIME_RANGE";

    static final public String INVALID_CHECKOUT_SESSION = "INVALID_CHECKOUT_SESSION";

    static final public String INVALID_REFEREE_ROLE = "INVALID_REFEREE_ROLE";

    static final public String INVALID_BIRTHDATE_FORMAT = "INVALID_BIRTHDATE_FORMAT";

    static final public String TOURNAMENT_ID_MISMATCH = "TOURNAMENT_ID_MISMATCH";

    // Conflict errors

    static final public String PLAYER_IS_IN_ACTIVE_TOURNAMENTS = "PLAYER_IS_IN_ACTIVE_TOURNAMENTS";

    static final public String CAPTAIN_CANNOT_LEAVE_TEAM_WITHOUT_VICE_CAPTAIN = "CAPTAIN_CANNOT_LEAVE_TEAM_WITHOUT_VICE_CAPTAIN";

    static final public String TEAM_ALREADY_HAS_VICE_CAPTAINS = "TEAM_ALREADY_HAS_VICE_CAPTAINS";

    static final public String PLAYER_ALREADY_IN_TEAM = "PLAYER_ALREADY_IN_TEAM";

    static final public String ORGANIZATION_NEED_ORGANIZATION_NAME = "ORGANIZATION_NEED_ORGANIZATION_NAME";

    static final public String CAPTAIN_OR_ONE_VICE_CAPTAIN_MUST_BE_CALLED_UP = "CAPTAIN_OR_ONE_VICE_CAPTAIN_MUST_BE_CALLED_UP";

    static final public String TOURNAMENT_NOT_IN_CREATED_STATUS = "TOURNAMENT_NOT_IN_CREATED_STATUS";

    static final public String NUM_TEAMS_DIFFERS_FROM_PAID_TEAMS = "NUM_TEAMS_DIFFERS_FROM_PAID_TEAMS";

    static final public String ACCOUNT_ALREADY_EXISTS = "ACCOUNT_ALREADY_EXISTS";

    static final public String NEED_TO_BE_IN_TEAM = "NEED_TO_BE_IN_TEAM";

    static final public String TEAM_MUST_PARTECIPATE_TO_TOURNAMENT = "TEAM_MUST_PARTECIPATE_TO_TOURNAMENT";

    static final public String CHECKOUT_SESSION_IS_NOT_PENDING = "CHECKOUT_SESSION_IS_NOT_PENDING";

    static final public String FIELD_NOT_IN_CITY = "FIELD_NOT_IN_CITY";

    static final public String FIELD_NOT_AVAILABLE = "FIELD_NOT_AVAILABLE";

    static final public String PLAYER_IS_CAPTAIN = "PLAYER_IS_CAPTAIN";

    static final public String TOURNAMENT_ALREADY_FULL = "TOURNAMENT_ALREADY_FULL";

    static final public String TOURNAMENT_ALREADY_STARTED = "TOURNAMENT_ALREADY_STARTED";

    static final public String TEAM_SPORT_CATEGORY_MUST_MATCH_TOURNAMENT_SPORT_CATEGORY = "TEAM_SPORT_CATEGORY_MUST_MATCH_TOURNAMENT_SPORT_CATEGORY";

    static final public String PLAYER_NOT_IN_TEAM = "PLAYER_NOT_IN_TEAM";

    static final public String TOURNAMENT_PARTICIPATION_ALREADY_PAID = "TOURNAMENT_PARTICIPATION_ALREADY_PAID";

    // Authentication errors

    static final public String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    static final public String INVALID_TOKEN = "INVALID_TOKEN";

    static final public String UNAUTHORIZED = "UNAUTHORIZED";

    static final public String FORBIDDEN = "FORBIDDEN";

    static final public String PLAYER_ROLE_REQUIRED = "PLAYER_ROLE_REQUIRED";

    static final public String COORDINATOR_ROLE_REQUIRED = "COORDINATOR_ROLE_REQUIRED";

    static final public String REFEREE_ROLE_REQUIRED = "REFEREE_ROLE_REQUIRED";

    static final public String ORGANIZATION_ROLE_REQUIRED = "ORGANIZATION_ROLE_REQUIRED";

    static final public String ADMIN_ROLE_REQUIRED = "ADMIN_ROLE_REQUIRED";

    static final public String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";

    static final public String YOU_MUST_BE_CAPTAIN_OR_VICE_CAPTAIN = "YOU_MUST_BE_CAPTAIN_OR_VICE_CAPTAIN";

    static final public String YOU_MUST_BE_CAPTAIN = "YOU_MUST_BE_CAPTAIN";

    static final public String YOU_MUST_BE_VICE_CAPTAIN = "YOU_MUST_BE_VICE_CAPTAIN";

    static final public String YOU_MUST_OWN_THE_BOOKING = "YOU_MUST_OWN_THE_BOOKING";

    // PAYMENT ERRORS
    static final public String PAYMENT_METHOD_NOT_SUPPORTED = "PAYMENT_METHOD_NOT_SUPPORTED";

    static final public String PAYMENT_AMOUNT_MUST_BE_POSITIVE = "PAYMENT_AMOUNT_MUST_BE_POSITIVE";

    static final public String PAYMENT_FAILED = "PAYMENT_FAILED";

    static final public String PAYMENT_REQUIRED = "PAYMENT_REQUIRED";

    static final public String PURCHASE_NOT_REFUNDABLE = "PURCHASE_NOT_REFUNDABLE";
}
