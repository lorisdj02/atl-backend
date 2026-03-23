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

    // Bad request errors

    static final public String UNSUPPORTED_SPORT_CATEGORY = "UNSUPPORTED_SPORT_CATEGORY";

    static final public String UNSUPPORTED_BOOKING_TYPE = "UNSUPPORTED_BOOKING_TYPE";

    static final public String INVALID_ACCOUNT_ROLE = "INVALID_ACCOUNT_ROLE";

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

    // Authentication errors

    static final public String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    static final public String INVALID_TOKEN = "INVALID_TOKEN";

    static final public String UNAUTHORIZED = "UNAUTHORIZED";

    static final public String FORBIDDEN = "FORBIDDEN";

}
