package com.atl.backend.config;

public class ApiJsonKeys {

    public static final String error = "error"; // String

    // OrganizationHomeScreen
    public static final String lobbyBookingsCache = "lobbyBookingsCache"; // Map<int, LobbyBooking>
    // OrganizationHomeScreen
    public static final String privateBookingsCache = "privateBookingsCache"; // Map<int, PrivateBooking>
    // OrganizationHomeScreen
    public static final String matchBookingsCache = "matchBookingsCache"; // Map<int, MatchBooking>

    public static final String lobbyBooking = "lobbyBooking"; // LobbyBooking

    public static final String numMatches = "numMatches"; // int

    // PlayerHomeScreen,
    public static final String bookings = "bookings"; // List<dynamic>
    // PlayerHomeScreen, CoordinatorHomeScreen
    public static final String activeTournaments = "activeTournaments"; // List<Tournament>
    // PlayerHomeScreen, CoordinatorHomeScreen, RefereeHomeScreen
    public static final String minutesRoundPerTournament = "minutesRoundPerTournament"; // Map<int, int>
    // PlayerHomeScreen, CoordinatorHomeScreen, RefereeHomeScreen
    public static final String minutesPausePerTournament = "minutesPausePerTournament"; // Map<int, int>
    // PlayerHomeScreen, CoordinatorHomeScreen
    public static final String footballMatchesPerTournament = "footballMatchesPerTournament"; // Map<int, List<FootballMatch>>
    // PlayerHomeScreen,
    public static final String footballStandingsPerTournament = "footballStandingsPerTournament"; // Map<int,
                                                                                           // List<FootballStanding>>

    // PlayerHomeScreen, CoordinatorHomeScreen, RefereeHomeScreen,
    // OrganizationHomeScreen, TeamScreen
    public static final String teamsCache = "teamsCache"; // Map<int, Team>
    // PlayerHomeScreen,
    public static final String accountTeam = "accountTeam"; // Team?
    // PlayerHomeScreen,
    public static final String maxPlayersPerLobby = "maxPlayersPerLobby"; // Map<int, int>
    // PlayerHomeScreen,
    public static final String currentPlayersPerLobby = "currentPlayersPerLobby"; // Map<int, int>
    // PlayerHomeScreen,
    public static final String completeAddressPerField = "completeAddressPerField"; // Map<int, String>
    // PlayerHomeScreen,
    public static final String playersPaidPerBooking = "playersPaidPerBooking"; // Map<int, int>

    // RefereeHomeScreen
    public static final String assignments = "assignments"; // List<RefereeAssignment>
    // RefereeHomeScreen
    public static final String assignedFootballMatches = "assignedFootballMatches"; // List<FootballMatch>

    // OrganizationHomeScreen
    public static final String generalBookings = "generalBookings"; // List<Booking>
    // OrganizationHomeScreen
    public static final String cityNamePerField = "cityNamePerField"; // Map<int, String>
    // OrganizationHomeScreen
    public static final String detailedBookings = "detailedBookings"; // Map<int, dynamic>
    // OrganizationHomeScreen
    public static final String footballMatchPerBooking = "footballMatchPerBooking"; // Map<int, FootballMatch>
    // OrganizationHomeScreen, PlayerHomeScreen
    public static final String usernamePerBooking = "usernamePerBooking"; // Map<int, String>
    // OrganizationHomeScreen, CreateTournamentScreen, BookFieldScreen
    public static final String fields = "fields"; // List<Field>

    // TeamsScreen
    public static final String captainNamePerTeam = "captainNamePerTeam"; // Map<int, String>

    // AbstractTeamScreen
    public static final String team = "team"; // Team
    // AbstractTeamScreen, FootballTournamentScreen
    public static final String players = "players"; // List<Player>
    // AbstractTeamScreen
    public static final String palmares = "palmares"; // List<Palmares>
    // AbstractTeamScreen, AllTournamentsScreen
    public static final String tournaments = "tournaments"; // List<Tournament>
    // AbstractTeamScreen
    public static final String coordinatorsCache = "coordinatorsCache"; // Map<int, Coordinator>
    // AbstractTeamScreen
    public static final String fieldsCache = "fieldsCache"; // Map<int, Field>
    // AbstractTeamScreen, FootballTournamentScreen
    public static final String footballPlayerStatsCache = "footballPlayerStatsCache"; // Map<int, FootballTeamStats>
    // AbstractTeamScreen
    public static final String historicalPlayers = "historicalPlayers"; // List<Player>

    // JoinLobbyScreen, AllTournamentsScreen, CreateTournamentScreen
    public static final String cities = "cities"; // List<City>

    // JoinLobbyScreen
    public static final String lobbies = "lobbies"; // List<LobbyBooking>

    // PlayerHomeScreen
    public static final String privateBookings = "privateBookings"; // List<PrivateBooking>


    // AllTournamentsScreen
    public static final String myTournaments = "myTournaments"; // List<Tournament>
    // AllTournamentsScreen
    public static final String coordinatorPertournament = "coordinatorPertournament"; // Map<int, Coordinator>
    // AllTournamentsScreen
    public static final String participatingTournaments = "participatingTournaments"; // List<Tournament>
    // AllTournamentsScreen
    public static final String favoriteTournaments = "favoriteTournaments"; // List<Tournament>
    // AllTournamentsScreen
    public static final String prizeMin = "prizeMin"; // double
    // AllTournamentsScreen
    public static final String prizeMax = "prizeMax"; // double
    // AllTournamentsScreen
    public static final String feeMin = "feeMin"; // double
    // AllTournamentsScreen
    public static final String feeMax = "feeMax"; // double

    // TournamentDetailScreen
    public static final String tournament = "tournament"; // Tournament
    // TournamentDetailScreen
    public static final String field = "field"; // Field
    // TournamentDetailScreen
    public static final String cityName = "cityName"; // String
    // TournamentDetailScreen
    public static final String currentPlayer = "currentPlayer"; // Player?
    // TournamentDetailScreen
    public static final String coordinator = "coordinator"; // Coordinator?
    // TournamentDetailScreen
    public static final String teamParticipation = "teamParticipation"; // TournamentParticipation?

    // CallUpsScreen
    public static final String sport = "sport"; // Sport
    // CallUpsScreen
    public static final String calledUpPlayerIds = "calledUpPlayerIds"; // List<int>

    // FootballTournamentScreen
    public static final String matches = "matches"; // List<FootballMatch>
    // FootballTournamentScreen
    public static final String standings = "standings"; // List<FootballStanding>
    // FootballTournamentScreen, FootballMatchScreen
    public static final String minutesPerRound = "minutesPerRound"; // int
    // FootballTournamentScreen, FootballMatchScreen
    public static final String minutesPerPause = "minutesPerPause"; // int
    // FootballTournamentScreen,
    public static final String finalStageBrackets = "finalStageBrackets"; // List<FinalStageBracket>

    // FootballMatchScreen
    public static final String match = "match"; // FootballMatch
    // FootballMatchScreen
    public static final String events = "events"; // List<FootballMatchEvent>
    // FootballMatchScreen, MatchBracket
    public static final String homeTeam = "homeTeam"; // Team?
    // FootballMatchScreen, MatchBracket
    public static final String awayTeam = "awayTeam"; // Team?
    // FootballMatchScreen
    public static final String availableRefereeRoles = "availableRefereeRoles"; // List<RefereeRole>
    // FootballMatchScreen
    public static final String playersPerTeam = "playersPerTeam"; // Map<int, List<Player>>
    // FootballMatchScreen
    public static final String assignmentPerRefereeNameSurname = "assignmentPerRefereeNameSurname"; // Map<int,
                                                                                             // List<RefereeAssignment>>
                                                                                             // // key: Name+Surname,
                                                                                             // value: Role
    // FootballMatchScreen
    public static final String coordinatorId = "coordinatorId"; // int
    // FootballMatchScreen
    public static final String refereeCheck = "refereeCheck"; // bool
    // FootballMatchScreen
    public static final String canMatchFinish = "canMatchFinish"; // bool
    // FootballMatchScreen
    public static final String hasRefereeLicense = "hasRefereeLicense"; // bool
    // FootballMatchScreen
    public static final String payPerRole = "payPerRole"; // Map<String, double>
    // FootballMatchScreen
    public static final String finalStageBracket = "finalStageBracket"; // FinalStageBracket?


    // PayParticipationScreen
    public static final String tournamentsCache = "tournamentsCache"; // Map<int, Tournament>
    // PayParticipationScreen
    public static final String tournamentParticipations = "tournamentParticipations"; // List<TournamentParticipation>
    // PayParticipationScreen
    public static final String isCalledUpPerTournament = "isCalledUpPerTournament"; // Map<int, bool>

    // LoginScreen
    public static final String account = "account"; // Account?
    // LoginScreen
    public static final String roleId = "roleId"; // int?
    // LoginScreen
    public static final String token = "token"; // String


    // ProfileScreen
    public static final String level = "level"; // String

    // CreateTournamentScreen
    public static final String fee = "fee"; // double
    // CreateTournamentScreen
    public static final String coordinatorPay = "coordinatorPay"; // double
    // CreateTournamentScreen
    public static final String prize1 = "prize1"; // double
    // CreateTournamentScreen
    public static final String prize2 = "prize2"; // double
    // CreateTournamentScreen
    public static final String maxPlayersPerField = "maxPlayersPerField"; // Map<int, int>

    // PlayerRequestsScreen
    public static final String teamRequests = "teamRequests"; // List<TeamRequest>
    // PlayerRequestsScreen
    public static final String playersCache = "playersCache"; // Map<int, Player?>

    // EditInfosScreen
    public static final String updatedAccount = "updatedAccount"; // Account?

    // WalletScreen
    public static final String wallet = "wallet"; // Wallet
    // WalletScreen
    public static final String transactionsHistory = "transactionsHistory"; // List<Transaction>


    // TeamDetailScreen
    public static final String isPlayerInTeam = "isPlayerInTeam"; // bool
    // TeamDetailScreen
    public static final String existingRequest = "existingRequest"; // TeamRequest?

    // MatchBracket
    public static final String winnerId = "winnerId"; // int

    // SportSelectorWidget
    public static final String sports = "sports"; // List<Sport>

    // RefereeLicensesScreen
    public static final String refereeLicenses = "refereeLicenses"; // List<RefereeLicense>

    // AvailableMatchesScreen
    public static final String footballMatchesPerSport = "footballMatchesPerSport"; // Map<int, List<FootballMatch>>
    // AvailableMatchesScreen
    public static final String sportNames = "sportNames"; // Map<int, String>

    // StatsScreen
    public static final String statsPerCategory = "statsPerCategory"; // Map<SportCategory, dynamic>
    // StatsScreen
    public static final String numTournamentPerCategory = "numTournamentPerCategory"; // Map<SportCategory, int>
    // StatsScreen
    public static final String numLobbyJoinedPerCategory = "numLobbyJoinedPerCategory"; // Map<SportCategory, int>
    // StatsScreen
    public static final String numPrivateBookingPerCategory = "numPrivateBookingPerCategory"; // Map<SportCategory, int>

    // Bottom bars
    public static final String hasUnreadNotifications = "hasUnreadNotifications"; // bool

    // ReviewsScreen
    public static final String reviews = "reviews"; // List<Review>
    // ReviewsScreen
    public static final String accountPerReview = "accountPerReview"; // Map<int, Account>
    // ReviewsScreen
    public static final String matchTeamsStringPerReview = "matchTeamsStringPerReview"; // Map<int, String>

    // InviteFriendsScreen
    public static final String referralCode = "referralCode"; // String
    // InviteFriendsScreen
    public static final String inviteBalance = "inviteBalance"; // double
    // InviteFriendsScreen
    public static final String numRedeemed = "numRedeemed"; // int
    // InviteFriendsScreen
    public static final String numInvites = "numInvites"; // int

    // CheckoutScreen
    public static final String checkoutSession = "checkoutSession"; // CheckoutSession
    // CheckoutScreen
    public static final String tournamentName = "tournamentName"; // String
    // CheckoutScreen
    public static final String fieldName = "fieldName"; // String
    // CheckoutScreen
    public static final String fieldAddress = "fieldAddress"; // String
    // CheckoutScreen
    public static final String bookingUsername = "bookingUsername"; // String
    // CheckoutScreen
    public static final String playersToPay = "playersToPay"; // int
    // CheckoutScreen
    public static final String startTime = "startTime"; // DateTime
    // CheckoutScreen
    public static final String finishTime = "finishTime"; // DateTime
    // CheckoutScreen
    public static final String maxPlayers = "maxPlayers"; // int

    // BookFieldScreen
    public static final String minOpeningTimePerCity = "minOpeningTimePerCity"; // Map<int, TimeOfDay>
    // BookFieldScreen
    public static final String maxClosingTimePerCity = "maxClosingTimePerCity"; // Map<int, TimeOfDay>

    // GrowthScreen
    public static final String growthSteps = "growthSteps"; // List<GrowthStep>
    // GrowthScreen
    public static final String minRefereePayPerCategoryPerLevel = "minRefereePayPerCategoryPerLevel"; // Map<int,
                                                                                               // Map<SportCategory,
                                                                                               // double>>
    // Growth Screen
    public static final String maxRefereePayPerCategoryPerLevel = "maxRefereePayPerCategoryPerLevel"; // Map<int,
                                                                                               // Map<SportCategory,
                                                                                               // double>>

    // Login Screen
    public static final String teamId = "teamId"; // int?

    // AvailableMatchesScreen
    public static final String footballMatches = "footballMatches"; // List<FootballMatch>

    // MatchBracket -> inneficiente
    public static final String footballMatch = "footballMatch"; // FootballMatch

    // StatsScreen
    public static final String footballStats = "footballStats"; // List<FootballTeamStats>
}
