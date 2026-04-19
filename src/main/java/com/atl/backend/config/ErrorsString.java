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

    static final public String TEAM_REQUEST_NOT_FOUND = "TEAM_REQUEST_NOT_FOUND";

    static final public String STANDING_NOT_FOUND = "STANDING_NOT_FOUND";

    static final public String REVIEW_NOT_FOUND = "REVIEW_NOT_FOUND";

    static final public String FINAL_STAGE_BRACKET_NOT_FOUND = "FINAL_STAGE_BRACKET_NOT_FOUND";

    static final public String TEAM_STAT_NOT_FOUND = "TEAM_STAT_NOT_FOUND";

    static final public String REFEREE_LICENSE_NOT_FOUND = "REFEREE_LICENSE_NOT_FOUND";

    // Bad request errors

    static final public String UNSUPPORTED_SPORT_CATEGORY = "UNSUPPORTED_SPORT_CATEGORY";

    static final public String UNSUPPORTED_BOOKING_TYPE = "UNSUPPORTED_BOOKING_TYPE";

    static final public String INVALID_ACCOUNT_ROLE = "INVALID_ACCOUNT_ROLE";

    static final public String INVALID_TIME_RANGE = "INVALID_TIME_RANGE";

    static final public String INVALID_CHECKOUT_SESSION = "INVALID_CHECKOUT_SESSION";

    static final public String INVALID_REFEREE_ROLE = "INVALID_REFEREE_ROLE";

    static final public String INVALID_BIRTHDATE_FORMAT = "INVALID_BIRTHDATE_FORMAT";

    static final public String TOURNAMENT_ID_MISMATCH = "TOURNAMENT_ID_MISMATCH";

    static final public String INVALID_TEAM_REQUEST_PARAMETERS = "INVALID_TEAM_REQUEST_PARAMETERS";

    static final public String PAYMENT_AMOUNT_EXCEEDS_TO_PAY = "PAYMENT_AMOUNT_EXCEEDS_TO_PAY";

    static final public String INVALID_TOURNAMENT_PHASE = "INVALID_TOURNAMENT_PHASE";

    static final public String INVALID_MATCH_SCHEDULE = "INVALID_MATCH_SCHEDULE";

    static final public String INVALID_CHECKOUT_TYPE = "INVALID_CHECKOUT_TYPE";

    static final public String INVALID_CHECKOUT_AMOUNT = "INVALID_CHECKOUT_AMOUNT";

    static final public String INVALID_REVIEW_RATING = "INVALID_REVIEW_RATING";

    static final public String MISSING_REQUIRED_FIELDS = "MISSING_REQUIRED_FIELDS";

    static final public String INVALID_FOOTBALL_EVENT_TYPE = "INVALID_FOOTBALL_EVENT_TYPE";

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

    static final public String PURCHASE_NOT_REDEEMABLE = "PURCHASE_NOT_REDEEMABLE";

    static final public String TEAM_REQUEST_ALREADY_SENT = "TEAM_REQUEST_ALREADY_SENT";

    static final public String TEAM_REQUEST_ALREADY_ACCEPTED = "TEAM_REQUEST_ALREADY_ACCEPTED";

    static final public String TEAM_REQUEST_ALREADY_REFUSED = "TEAM_REQUEST_ALREADY_REFUSED";

    static final public String BOOKING_NOT_REFUNDABLE = "BOOKING_NOT_REFUNDABLE";

    static final public String BOOKING_ALREADY_STARTED = "BOOKING_ALREADY_STARTED";

    static final public String BOOKING_ALREADY_FINISHED = "BOOKING_ALREADY_FINISHED";

    static final public String TEAM_WITHOUT_CAPTAIN_AND_VICE_CAPTAINS = "TEAM_WITHOUT_CAPTAIN_AND_VICE_CAPTAINS";

    static final public String ACCOUNT_HAS_REVIEWS = "ACCOUNT_HAS_REVIEWS";

    static final public String COORDINATOR_HAS_ACTIVE_TOURNAMENTS = "COORDINATOR_HAS_ACTIVE_TOURNAMENTS";

    static final public String REFEREE_HAS_ACTIVE_MATCHES = "REFEREE_HAS_ACTIVE_MATCHES";

    static final public String TEAM_NOT_IN_MATCH = "TEAM_NOT_IN_MATCH";

    static final public String MATCH_NOT_IN_KNOCKOUT_PHASE = "MATCH_NOT_IN_KNOCKOUT_PHASE";

    static final public String WRONG_TOURNAMENT_PHASE = "WRONG_TOURNAMENT_PHASE";

    static final public String MATCH_SCHEDULE_TOO_LATE = "MATCH_SCHEDULE_TOO_LATE";

    static final public String TOURNAMENT_NOT_ACTIVE = "TOURNAMENT_NOT_ACTIVE";

    static final public String MATCH_ALREADY_SCHEDULED_FOR_THIS_TIME = "MATCH_ALREADY_SCHEDULED_FOR_THIS_TIME";

    static final public String YOU_CANNOT_RESCHEDULE_MATCH_LESS_THAN_24_HOURS_BEFORE = "YOU_CANNOT_RESCHEDULE_MATCH_LESS_THAN_24_HOURS_BEFORE";

    static final public String MOTM_ALREADY_ASSIGNED = "MOTM_ALREADY_ASSIGNED";

    static final public String NEGATIVE_AMOUNT_TRANSACTION = "NEGATIVE_AMOUNT_TRANSACTION";

    static final public String MATCH_NOT_SCHEDULED = "MATCH_NOT_SCHEDULED";

    static final public String MATCH_ALREADY_HAS_ENOUGH_REFEREES = "MATCH_ALREADY_HAS_ENOUGH_REFEREES";

    static final public String CANNOT_START_PENALTY_SHOOTOUT = "CANNOT_START_PENALTY_SHOOTOUT";

    static final public String REFEREE_LICENSE_MISMATCH = "REFEREE_LICENSE_MISMATCH";

    static final public String VICE_CAPTAIN_CANNOT_EXPEL_VICE_CAPTAIN = "VICE_CAPTAIN_CANNOT_EXPEL_VICE_CAPTAIN";

    static final public String CAPTAIN_CANNOT_BE_EXPELLED = "CAPTAIN_CANNOT_BE_EXPELLED";

    static final public String NO_AVAILABLE_SPOTS = "NO_AVAILABLE_SPOTS";

    static final public String TRANSACTION_FAILED = "TRANSACTION_FAILED";

    static final public String CANNOT_FINISH_MATCH = "CANNOT_FINISH_MATCH";

    static final public String CANNOT_START_MATCH = "CANNOT_START_MATCH";

    static final public String USERNAME_ALREADY_EXISTS = "USERNAME_ALREADY_EXISTS";

    static final public String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";

    static final public String BOOKING_NOT_IN_BOOKED_STATUS = "BOOKING_NOT_IN_BOOKED_STATUS";

    static final public String TOURNAMENT_NOT_IN_FINAL_STAGE = "TOURNAMENT_NOT_IN_FINAL_STAGE";

    static final public String TOURNAMENT_HAS_UNFINISHED_MATCHES = "TOURNAMENT_HAS_UNFINISHED_MATCHES";

    static final public String TOURNAMENT_NOT_FINISHED = "TOURNAMENT_NOT_FINISHED";

    static final public String WINNING_TEAM_NOT_FOUND = "WINNING_TEAM_NOT_FOUND";

    static final public String TOURNAMENT_NOT_PENDING_PAYMENTS = "TOURNAMENT_NOT_PENDING_PAYMENTS";

    static final public String MATCH_ALREADY_FINISHED = "MATCH_ALREADY_FINISHED";

    static final public String MATCH_ALREADY_STARTED = "MATCH_ALREADY_STARTED";

    static final public String CANNOT_SCHEDULE_MATCH = "CANNOT_SCHEDULE_MATCH";

    static final public String CANNOT_START_MATCH_BEFORE_SCHEDULED_TIME = "CANNOT_START_MATCH_BEFORE_SCHEDULED_TIME";

    static final public String CANNOT_ADD_EVENT_TO_MATCH = "CANNOT_ADD_EVENT_TO_MATCH";

    static final public String PLAYER_HAS_RED_CARD = "PLAYER_HAS_RED_CARD";

    static final public String CANNOT_START_HALF = "CANNOT_START_HALF";

    static final public String CANNOT_GO_HALFTIME = "CANNOT_GO_HALFTIME";

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

    static final public String PLAYER_NOT_CALLED_UP_IN_TOURNAMENT = "PLAYER_NOT_CALLED_UP_IN_TOURNAMENT";

    static final public String NOT_ALL_MATCHES_FINISHED = "NOT_ALL_MATCHES_FINISHED";

    static final public String YOU_MUST_BE_TOURNAMENT_COORDINATOR = "YOU_MUST_BE_TOURNAMENT_COORDINATOR";

    // PAYMENT ERRORS
    static final public String PAYMENT_METHOD_NOT_SUPPORTED = "PAYMENT_METHOD_NOT_SUPPORTED";

    static final public String PAYMENT_AMOUNT_MUST_BE_POSITIVE = "PAYMENT_AMOUNT_MUST_BE_POSITIVE";

    static final public String PAYMENT_FAILED = "PAYMENT_FAILED";

    static final public String PAYMENT_REQUIRED = "PAYMENT_REQUIRED";

    static final public String PURCHASE_NOT_REFUNDABLE = "PURCHASE_NOT_REFUNDABLE";

    // Other errors
    static final public String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    static final public String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";

    static final public String GATEWAY_TIMEOUT = "GATEWAY_TIMEOUT";

    static final public String UNKNOWN_ERROR = "UNKNOWN_ERROR";

    static final public String INVALID_NUMBER_OF_QUALIFIED_TEAMS = "INVALID_NUMBER_OF_QUALIFIED_TEAMS";
}
