package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class BefccRepository(
    private val database: AppDatabase,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val userDao = database.userDao()
    private val tournamentDao = database.tournamentDao()
    private val slotDao = database.slotDao()
    private val matchDao = database.matchDao()
    private val standingDao = database.standingDao()
    private val notificationDao = database.notificationDao()
    private val systemSettingDao = database.systemSettingDao()

    // Current Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(nulAuthenticarrentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        externalScope.launch {
            try {
                seedDatabaseIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isInitialized.value = true
            }
        }
    }

    private suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingLeader = userDao.getUserByIdOnce(InitialData.leaderUser.id)
        if (existingLeader == null) {
            userDao.insertUsers(InitialData.adminTeam)
            notificationDao.insertNotifications(InitialData.initialNotifications)
        }
    }

    // --- Authentication ---

suspend fun login(
    emailOrUsername: String,
    pass: String
): Result<UserEntity> = withContext(Dispatchers.IO) {

    val query = emailOrUsername.trim()

    var user: UserEntity? = if (query.contains("@")) {
        userDao.getUserByEmail(query.lowercase())
    } else {
        userDao.getUserByUsername(query)
    }

    // Fixed Super Admin login
    if (
        user == null &&
        (
            query.equals("maruf", ignoreCase = true) ||
            query.equals("maruf_leader", ignoreCase = true) ||
            query.equals("maruf@befcc.org", ignoreCase = true)
        )
    ) {
        seedDatabaseIfEmpty()

        user = userDao.getUserByIdOnce(
            InitialData.leaderUser.id
        ) ?: InitialData.leaderUser
    }

    if (user == null) {
        return@withContext Result.failure(
            Exception("Account not found.")
        )
    }

    // Admin users
    if (
        user.role == UserRole.SUPER_ADMIN ||
        user.role == UserRole.ADMIN
    ) {
        _currentUser.value = user

        return@withContext Result.success(user)
    }

    // Normal Player
    if (user.role == UserRole.PLAYER) {
        _currentUser.value = user

        return@withContext Result.success(user)
    }

    Result.failure(
        Exception("Unauthorized account.")
    )
}
    suspend fun registerUser(
    fullName: String,
    username: String,
    email: String,
    inGameUsername: String,
    favoriteTeam: String,
    divisionRank: String = "Division 2"
): Result<UserEntity> = withContext(Dispatchers.IO) {

    val cleanName = fullName.trim()
    val cleanUsername = username.trim()
    val cleanEmail = email.trim().lowercase()
    val cleanGameUsername = inGameUsername.trim()
    val cleanFavoriteTeam = favoriteTeam.trim().ifBlank { "Bangladesh" }

    // Basic validation
    if (cleanName.isBlank()) {
        return@withContext Result.failure(
            Exception("Player name is required.")
        )
    }

    if (cleanUsername.isBlank()) {
        return@withContext Result.failure(
            Exception("Username is required.")
        )
    }

    if (cleanEmail.isBlank()) {
        return@withContext Result.failure(
            Exception("Email is required.")
        )
    }

    if (cleanGameUsername.isBlank()) {
        return@withContext Result.failure(
            Exception("In-game username is required.")
        )
    }

    // Email must be unique
    val existingEmail = userDao.getUserByEmail(cleanEmail)

    if (existingEmail != null) {
        return@withContext Result.failure(
            Exception("An account with this email already exists.")
        )
    }

    // Username must be unique
    val existingUsername = userDao.getUserByUsername(cleanUsername)

    if (existingUsername != null) {
        return@withContext Result.failure(
            Exception("Username is already taken.")
        )
    }

    // Generate unique BEFCC Player ID
    var newPlayerId: String
    var existingPlayer: UserEntity?

    do {
        val randomSuffix = (10000..99999).random()
        newPlayerId = "BEFCC-$randomSuffix"
        existingPlayer = userDao.getUserByIdOnce(newPlayerId)
    } while (existingPlayer != null)

    // Create every new account as NORMAL PLAYER.
    // Nobody becomes Admin automatically.
    val newUser = UserEntity(
        id = "user_${UUID.randomUUID().toString().take(8)}",
        email = cleanEmail,
        fullName = cleanName,
        username = cleanUsername,
        playerId = newPlayerId,
        role = UserRole.PLAYER,
        avatarName = "avatar_${(1..8).random()}",
        inGameUsername = cleanGameUsername,
        favoriteTeam = cleanFavoriteTeam,
        divisionRank = divisionRank,
        matchesPlayed = 0,
        wins = 0,
        draws = 0,
        losses = 0,
        goalsScored = 0,
        goalsConceded = 0,
        points = 0,
        achievements = "BEFCC Verified Player",
        selectedTeams = cleanFavoriteTeam,
        createdAt = System.currentTimeMillis()
    )

    // Save user
    userDao.insertUser(newUser)

    // Set newly registered player as current user
    _currentUser.value = newUser

    // Welcome notification
    notificationDao.insertNotification(
        NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = newUser.id,
            title = "Welcome to BEFCC, ${newUser.fullName}!",
            message = "Your player registration is complete. Your official Player ID is ${newUser.playerId}.",
            type = NotificationType.ACCOUNT_CREATED,
            timestamp = System.currentTimeMillis()
        )
    )

    Result.success(newUser)
}

    suspend fun continueWithGoogle(
        googleName: String,
        googleEmail: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(googleEmail.trim().lowercase())
        if (existing != null) {
            _currentUser.value = existing
            return@withContext Result.success(existing)
        }

        val baseUsername = googleEmail.substringBefore("@").replace(".", "_")
        val randomSuffix = (10000..99999).random()
        val newPlayerId = "BEFCC-$randomSuffix"
        val newUser = UserEntity(
            id = "user_g_${UUID.randomUUID().toString().take(8)}",
            email = googleEmail.trim().lowercase(),
            fullName = googleName.ifBlank { "eFootball Player" },
            username = baseUsername,
            playerId = newPlayerId,
            role = UserRole.PLAYER,
            avatarName = "avatar_1",
            inGameUsername = baseUsername,
            favoriteTeam = "Bangladesh",
            divisionRank = "Division 2",
            matchesPlayed = 0,
            wins = 0,
            draws = 0,
            losses = 0,
            goalsScored = 0,
            goalsConceded = 0,
            points = 0,
            achievements = "BEFCC Verified Player",
            selectedTeams = "Bangladesh",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = newUser.id,
                title = "Google Sign-In Successful",
                message = "Welcome to BEFCC! Your official Player ID is ${newUser.playerId}.",
                type = NotificationType.ACCOUNT_CREATED
            )
        )

        Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
    }

    // Role-switching: Only allowed for Admins and Super Admin (Maruf Hossain / Leader)
    suspend fun switchRole(toAdmin: Boolean) = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext
        if (!current.isAdminOrLeader) {
            // Regular players cannot switch to admin
            return@withContext
        }
        if (toAdmin) {
            val leader = userDao.getUserByIdOnce(InitialData.leaderUser.id) ?: InitialData.leaderUser
            _currentUser.value = leader
        } else {
            // Temporary view as player
            val playerView = current.copy(role = UserRole.PLAYER)
            _currentUser.value = playerView
        }
    }

    suspend fun setCurrentUser(user: UserEntity) = withContext(Dispatchers.IO) {
        _currentUser.value = user
    }

    suspend fun updateProfile(
        fullName: String,
        inGameUsername: String,
        favoriteTeam: String,
        divisionRank: String,
        selectedTeams: String
    ) = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext
        val updated = current.copy(
            fullName = fullName,
            inGameUsername = inGameUsername,
            favoriteTeam = favoriteTeam,
            divisionRank = divisionRank,
            selectedTeams = selectedTeams
        )
        userDao.updateUser(updated)
        _currentUser.value = updated
    }

    // --- Data Observables ---
    fun getAllTournaments(): Flow<List<TournamentEntity>> = tournamentDao.getAllTournaments()
    fun getTournament(id: String): Flow<TournamentEntity?> = tournamentDao.getTournamentById(id)

    fun getSlotsForTournament(tournamentId: String): Flow<List<SlotEntity>> = slotDao.getSlotsByTournament(tournamentId)
    fun getSlotsForPlayer(playerId: String): Flow<List<SlotEntity>> = slotDao.getSlotsByPlayer(playerId)
    fun getAllSlots(): Flow<List<SlotEntity>> = slotDao.getAllSlots()

    fun getAllMatches(): Flow<List<MatchEntity>> = matchDao.getAllMatches()
    fun getMatchesForTournament(tournamentId: String): Flow<List<MatchEntity>> = matchDao.getMatchesByTournament(tournamentId)
    fun getMatchesForPlayer(playerId: String): Flow<List<MatchEntity>> = matchDao.getMatchesByPlayer(playerId)
    fun getPendingMatches(): Flow<List<MatchEntity>> = matchDao.getPendingMatches()

    fun getStandingsForTournament(tournamentId: String): Flow<List<StandingEntity>> = standingDao.getStandingsByTournament(tournamentId)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUser(userId)
    fun getNotificationsForAdmin(): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForAdmin()

    // --- Tournament Management (Admin) ---
    suspend fun createTournament(
        name: String,
        type: String,
        playerLimit: Int,
        entryFee: Double,
        prizePool: String,
        teamType: TournamentTeamType,
        availableTeams: String,
        rules: String,
        startDate: String,
        endDate: String
    ) = withContext(Dispatchers.IO) {
        val groupsCount = when (playerLimit) {
            12 -> 3
            16 -> 4
            20 -> 5
            24 -> 6
            28 -> 7
            32 -> 8
            36 -> 9
            40 -> 10
            44 -> 11
            48 -> 12
            else -> playerLimit / 4
        }
        val groupSize = 4

        val tournId = "tourn_${UUID.randomUUID().toString().take(8)}"
        val tournament = TournamentEntity(
            id = tournId,
            name = name,
            type = type,
            playerLimit = playerLimit,
            entryFee = entryFee,
            prizePool = prizePool,
            registrationStatus = "OPEN",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = startDate,
            endDate = endDate,
            teamType = teamType,
            availableTeams = availableTeams,
            rules = rules,
            groupsCount = groupsCount,
            groupSize = groupSize
        )
        tournamentDao.insertTournament(tournament)

        // Generate empty slots
        val slots = (1..playerLimit).map { slotNum ->
            SlotEntity(
                id = "${tournId}_slot_$slotNum",
                tournamentId = tournId,
                slotNumber = slotNum,
                status = SlotStatus.AVAILABLE
            )
        }
        slotDao.insertSlots(slots)

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = "ALL",
                title = "New Tournament Announced!",
                message = "$name ($playerLimit Players) is now open for registration. Book your slot now!",
                type = NotificationType.TOURNAMENT_REGISTERED
            )
        )
    }

    suspend fun updateTournamentStatus(tournamentId: String, status: TournamentStatus) = withContext(Dispatchers.IO) {
        val tourn = tournamentDao.getTournamentByIdOnce(tournamentId) ?: return@withContext
        val regStatus = if (status == TournamentStatus.REGISTRATION_OPEN) "OPEN" else "CLOSED"
        tournamentDao.updateTournament(tourn.copy(status = status, registrationStatus = regStatus))
    }

    suspend fun deleteTournament(tournamentId: String) = withContext(Dispatchers.IO) {
        tournamentDao.deleteTournament(tournamentId)
        slotDao.deleteSlotsByTournament(tournamentId)
        matchDao.deleteMatchesByTournament(tournamentId)
        standingDao.deleteStandingsByTournament(tournamentId)
    }

    // --- Slot Booking & Payment Verification ---
    suspend fun bookSlot(
        tournamentId: String,
        slotNumber: Int,
        selectedTeam: String,
        teamType: String,
        entryFee: Double,
        paymentMethod: String,
        transactionNumber: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        val existingSlots = slotDao.getSlotsByTournamentOnce(tournamentId)
        val targetSlot = existingSlots.find { it.slotNumber == slotNumber }
            ?: return@withContext Result.failure(Exception("Slot not found"))

        if (targetSlot.status == SlotStatus.CONFIRMED || targetSlot.status == SlotStatus.PENDING) {
            return@withContext Result.failure(Exception("Slot #$slotNumber is no longer available."))
        }

        // Check duplicate team
        val teamTaken = existingSlots.any {
            (it.status == SlotStatus.CONFIRMED || it.status == SlotStatus.PENDING) &&
                    it.selectedTeam.equals(selectedTeam, ignoreCase = true)
        }
        if (teamTaken) {
            return@withContext Result.failure(Exception("Team '$selectedTeam' has already been picked by another player."))
        }

        val updatedSlot = targetSlot.copy(
            playerId = user.id,
            playerName = user.fullName,
            playerUsername = user.username,
            selectedTeam = selectedTeam,
            teamType = teamType,
            entryFee = entryFee,
            paymentMethod = paymentMethod,
            transactionNumber = transactionNumber.trim().uppercase(),
            submissionTime = System.currentTimeMillis(),
            status = SlotStatus.PENDING,
            adminNotes = "Pending admin verification"
        )
        slotDao.updateSlot(updatedSlot)

        // Notification to User
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = "Slot Booking Submitted",
                message = "Slot #$slotNumber with Trx ID $transactionNumber submitted for verification.",
                type = NotificationType.SLOT_SUBMITTED
            )
        )

        // Notification to Admin
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = "ADMIN",
                title = "New Transaction Verification",
                message = "${user.fullName} submitted $paymentMethod Trx: $transactionNumber for slot #$slotNumber.",
                type = NotificationType.SLOT_SUBMITTED
            )
        )

        Result.success(Unit)
    }

    suspend fun verifySlot(
        slotId: String,
        approve: Boolean,
        adminNotes: String? = null
    ) = withContext(Dispatchers.IO) {
        val slot = slotDao.getSlotById(slotId) ?: return@withContext
        val newStatus = if (approve) SlotStatus.CONFIRMED else SlotStatus.REJECTED
        val updatedSlot = slot.copy(
            status = newStatus,
            adminNotes = adminNotes ?: if (approve) "Approved by BEFCC Admin" else "Transaction invalid or rejected"
        )
        slotDao.updateSlot(updatedSlot)

        slot.playerId?.let { pId ->
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = pId,
                    title = if (approve) "Slot Confirmed!" else "Slot Request Rejected",
                    message = if (approve)
                        "Your slot #${slot.slotNumber} (${slot.selectedTeam}) has been approved and confirmed."
                    else
                        "Your slot #${slot.slotNumber} was rejected: ${adminNotes ?: "Payment could not be verified."}",
                    type = if (approve) NotificationType.SLOT_APPROVED else NotificationType.SLOT_REJECTED
                )
            )
        }
    }

    // --- Group Stage & Fixture Generation ---
    suspend fun generateGroupsAndFixtures(tournamentId: String) = withContext(Dispatchers.IO) {
        val tournament = tournamentDao.getTournamentByIdOnce(tournamentId) ?: return@withContext
        val confirmedSlots = slotDao.getSlotsByTournamentOnce(tournamentId)
            .filter { it.status == SlotStatus.CONFIRMED }

        val groupsCount = tournament.groupsCount
        val groupNames = (0 until groupsCount).map { "Group " + ('A' + it) }

        // Clear existing standings & fixtures for this tournament
        standingDao.deleteStandingsByTournament(tournamentId)
        matchDao.deleteMatchesByTournament(tournamentId)

        val standings = mutableListOf<StandingEntity>()
        val groupPlayersMap = mutableMapOf<String, MutableList<SlotEntity>>()

        groupNames.forEach { groupPlayersMap[it] = mutableListOf() }

        // Distribute confirmed slots across groups
        confirmedSlots.forEachIndexed { index, slot ->
            val groupName = groupNames[index % groupsCount]
            groupPlayersMap[groupName]?.add(slot)

            // Update slot assigned group
            slotDao.updateSlot(slot.copy(assignedGroup = groupName))

            standings.add(
                StandingEntity(
                    id = "${tournamentId}_${groupName}_${slot.playerId ?: slot.slotNumber}",
                    tournamentId = tournamentId,
                    groupName = groupName,
                    playerId = slot.playerId ?: "player_${slot.slotNumber}",
                    playerName = slot.playerName ?: "Slot ${slot.slotNumber}",
                    playerTeam = slot.selectedTeam ?: "Team ${slot.slotNumber}",
                    played = 0,
                    won = 0,
                    drawn = 0,
                    lost = 0,
                    goalsFor = 0,
                    goalsAgainst = 0,
                    goalDifference = 0,
                    points = 0,
                    position = (groupPlayersMap[groupName]?.size ?: 1),
                    isQualified = false
                )
            )
        }

        standingDao.insertStandings(standings)

        // Generate round-robin fixtures per group
        val matches = mutableListOf<MatchEntity>()
        var matchCount = 1

        groupPlayersMap.forEach { (groupName, plist) ->
            for (i in 0 until plist.size) {
                for (j in (i + 1) until plist.size) {
                    val p1 = plist[i]
                    val p2 = plist[j]
                    matches.add(
                        MatchEntity(
                            id = "${tournamentId}_fixture_${matchCount++}",
                            tournamentId = tournamentId,
                            tournamentName = tournament.name,
                            matchType = MatchType.TOURNAMENT_GROUP,
                            roundStage = "$groupName - Matchday ${(i + j) % 3 + 1}",
                            groupName = groupName,
                            player1Id = p1.playerId ?: "p_${p1.slotNumber}",
                            player1Name = p1.playerName ?: "Slot ${p1.slotNumber}",
                            player1Team = p1.selectedTeam ?: "Team 1",
                            player2Id = p2.playerId ?: "p_${p2.slotNumber}",
                            player2Name = p2.playerName ?: "Slot ${p2.slotNumber}",
                            player2Team = p2.selectedTeam ?: "Team 2",
                            scheduledTime = "2026-09-${String.format("%02d", 2 + (matchCount % 7))} 20:00",
                            status = MatchStatus.SCHEDULED,
                            verificationStatus = "NOT_SUBMITTED"
                        )
                    )
                }
            }
        }

        matchDao.insertMatches(matches)

        tournamentDao.updateTournament(
            tournament.copy(
                groupsGenerated = true,
                fixturesGenerated = true,
                status = TournamentStatus.GROUP_STAGE,
                currentRound = "Group Stage"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = "ALL",
                title = "Groups & Fixtures Announced!",
                message = "Groups and match schedules for '${tournament.name}' are now generated.",
                type = NotificationType.MATCH_ASSIGNED
            )
        )
    }

    // --- Generate Knockout Bracket ---
    suspend fun generateKnockoutBracket(tournamentId: String) = withContext(Dispatchers.IO) {
        val tournament = tournamentDao.getTournamentByIdOnce(tournamentId) ?: return@withContext
        val allStandings = standingDao.getStandingsByTournamentOnce(tournamentId)

        // Pick top 2 from each group
        val qualifiedPlayers = allStandings
            .groupBy { it.groupName }
            .flatMap { (_, list) ->
                list.sortedWith(compareByDescending<StandingEntity> { it.points }
                    .thenByDescending { it.goalDifference }
                    .thenByDescending { it.goalsFor })
                    .take(2)
            }

        // Mark them as qualified in DB
        val updatedStandings = allStandings.map { s ->
            val isQ = qualifiedPlayers.any { it.playerId == s.playerId && it.groupName == s.groupName }
            s.copy(isQualified = isQ)
        }
        standingDao.insertStandings(updatedStandings)

        val knockoutMatches = mutableListOf<MatchEntity>()

        if (qualifiedPlayers.size >= 8) {
            // Quarter Finals -> Semi Finals -> Final
            val qf1 = MatchEntity(
                id = "${tournamentId}_qf_1",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Quarter Final 1",
                player1Id = qualifiedPlayers.getOrNull(0)?.playerId ?: "TBD",
                player1Name = qualifiedPlayers.getOrNull(0)?.playerName ?: "Group A #1",
                player1Team = qualifiedPlayers.getOrNull(0)?.playerTeam ?: "TBD",
                player2Id = qualifiedPlayers.getOrNull(3)?.playerId ?: "TBD",
                player2Name = qualifiedPlayers.getOrNull(3)?.playerName ?: "Group B #2",
                player2Team = qualifiedPlayers.getOrNull(3)?.playerTeam ?: "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-12 19:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "QF_1",
                nextBracketNodeId = "SF_1",
                nextBracketSlot = 1
            )
            val qf2 = MatchEntity(
                id = "${tournamentId}_qf_2",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Quarter Final 2",
                player1Id = qualifiedPlayers.getOrNull(2)?.playerId ?: "TBD",
                player1Name = qualifiedPlayers.getOrNull(2)?.playerName ?: "Group B #1",
                player1Team = qualifiedPlayers.getOrNull(2)?.playerTeam ?: "TBD",
                player2Id = qualifiedPlayers.getOrNull(1)?.playerId ?: "TBD",
                player2Name = qualifiedPlayers.getOrNull(1)?.playerName ?: "Group A #2",
                player2Team = qualifiedPlayers.getOrNull(1)?.playerTeam ?: "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-12 20:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "QF_2",
                nextBracketNodeId = "SF_1",
                nextBracketSlot = 2
            )
            val qf3 = MatchEntity(
                id = "${tournamentId}_qf_3",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Quarter Final 3",
                player1Id = qualifiedPlayers.getOrNull(4)?.playerId ?: "TBD",
                player1Name = qualifiedPlayers.getOrNull(4)?.playerName ?: "Group C #1",
                player1Team = qualifiedPlayers.getOrNull(4)?.playerTeam ?: "TBD",
                player2Id = qualifiedPlayers.getOrNull(7)?.playerId ?: "TBD",
                player2Name = qualifiedPlayers.getOrNull(7)?.playerName ?: "Group D #2",
                player2Team = qualifiedPlayers.getOrNull(7)?.playerTeam ?: "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-12 21:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "QF_3",
                nextBracketNodeId = "SF_2",
                nextBracketSlot = 1
            )
            val qf4 = MatchEntity(
                id = "${tournamentId}_qf_4",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Quarter Final 4",
                player1Id = qualifiedPlayers.getOrNull(6)?.playerId ?: "TBD",
                player1Name = qualifiedPlayers.getOrNull(6)?.playerName ?: "Group D #1",
                player1Team = qualifiedPlayers.getOrNull(6)?.playerTeam ?: "TBD",
                player2Id = qualifiedPlayers.getOrNull(5)?.playerId ?: "TBD",
                player2Name = qualifiedPlayers.getOrNull(5)?.playerName ?: "Group C #2",
                player2Team = qualifiedPlayers.getOrNull(5)?.playerTeam ?: "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-12 22:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "QF_4",
                nextBracketNodeId = "SF_2",
                nextBracketSlot = 2
            )

            val sf1 = MatchEntity(
                id = "${tournamentId}_sf_1",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Semi Final 1",
                player1Id = "TBD",
                player1Name = "Winner QF1",
                player1Team = "TBD",
                player2Id = "TBD",
                player2Name = "Winner QF2",
                player2Team = "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-14 19:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "SF_1",
                nextBracketNodeId = "FINAL",
                nextBracketSlot = 1
            )
            val sf2 = MatchEntity(
                id = "${tournamentId}_sf_2",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Semi Final 2",
                player1Id = "TBD",
                player1Name = "Winner QF3",
                player1Team = "TBD",
                player2Id = "TBD",
                player2Name = "Winner QF4",
                player2Team = "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-14 20:30",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "SF_2",
                nextBracketNodeId = "FINAL",
                nextBracketSlot = 2
            )

            val finalMatch = MatchEntity(
                id = "${tournamentId}_final",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Grand Final",
                player1Id = "TBD",
                player1Name = "Winner SF1",
                player1Team = "TBD",
                player2Id = "TBD",
                player2Name = "Winner SF2",
                player2Team = "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-15 21:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "FINAL"
            )

            knockoutMatches.addAll(listOf(qf1, qf2, qf3, qf4, sf1, sf2, finalMatch))
        } else {
            // Semi Finals & Final (for 4 qualified players)
            val sf1 = MatchEntity(
                id = "${tournamentId}_sf_1",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Semi Final 1",
                player1Id = qualifiedPlayers.getOrNull(0)?.playerId ?: "TBD",
                player1Name = qualifiedPlayers.getOrNull(0)?.playerName ?: "Group A #1",
                player1Team = qualifiedPlayers.getOrNull(0)?.playerTeam ?: "TBD",
                player2Id = qualifiedPlayers.getOrNull(3)?.playerId ?: "TBD",
                player2Name = qualifiedPlayers.getOrNull(3)?.playerName ?: "Group B #2",
                player2Team = qualifiedPlayers.getOrNull(3)?.playerTeam ?: "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-14 19:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "SF_1",
                nextBracketNodeId = "FINAL",
                nextBracketSlot = 1
            )
            val sf2 = MatchEntity(
                id = "${tournamentId}_sf_2",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Semi Final 2",
                player1Id = qualifiedPlayers.getOrNull(2)?.playerId ?: "TBD",
                player1Name = qualifiedPlayers.getOrNull(2)?.playerName ?: "Group B #1",
                player1Team = qualifiedPlayers.getOrNull(2)?.playerTeam ?: "TBD",
                player2Id = qualifiedPlayers.getOrNull(1)?.playerId ?: "TBD",
                player2Name = qualifiedPlayers.getOrNull(1)?.playerName ?: "Group A #2",
                player2Team = qualifiedPlayers.getOrNull(1)?.playerTeam ?: "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-14 20:30",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "SF_2",
                nextBracketNodeId = "FINAL",
                nextBracketSlot = 2
            )
            val finalMatch = MatchEntity(
                id = "${tournamentId}_final",
                tournamentId = tournamentId,
                tournamentName = tournament.name,
                matchType = MatchType.TOURNAMENT_KNOCKOUT,
                roundStage = "Grand Final",
                player1Id = "TBD",
                player1Name = "Winner SF1",
                player1Team = "TBD",
                player2Id = "TBD",
                player2Name = "Winner SF2",
                player2Team = "TBD",
                isKnockout = true,
                scheduledTime = "2026-09-15 21:00",
                status = MatchStatus.SCHEDULED,
                bracketNodeId = "FINAL"
            )
            knockoutMatches.addAll(listOf(sf1, sf2, finalMatch))
        }

        matchDao.insertMatches(knockoutMatches)

        tournamentDao.updateTournament(
            tournament.copy(
                knockoutBracketGenerated = true,
                status = TournamentStatus.KNOCKOUT_STAGE,
                currentRound = "Knockout Stage"
            )
        )
    }

    // --- Match Result Submission & Verification ---
    suspend fun submitMatchResult(
        matchId: String,
        p1Score: Int,
        p2Score: Int,
        p1Pens: Int? = null,
        p2Pens: Int? = null,
        notes: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        val match = matchDao.getMatchById(matchId) ?: return@withContext Result.failure(Exception("Match not found"))

        val updatedMatch = match.copy(
            player1Score = p1Score,
            player2Score = p2Score,
            player1Penalties = p1Pens,
            player2Penalties = p2Pens,
            matchNotes = notes,
            status = MatchStatus.PENDING_VERIFICATION,
            verificationStatus = "PENDING_VERIFICATION",
            submittedByPlayerId = user.id
        )
        matchDao.updateMatch(updatedMatch)

        // Notify Admin
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = "ADMIN",
                title = "Match Score Submitted",
                message = "${user.fullName} submitted score: ${match.player1Name} $p1Score - $p2Score ${match.player2Name}",
                type = NotificationType.RESULT_SUBMITTED
            )
        )

        Result.success(Unit)
    }

    suspend fun verifyMatchResult(
        matchId: String,
        approve: Boolean,
        correctedP1Score: Int? = null,
        correctedP2Score: Int? = null,
        correctedP1Pens: Int? = null,
        correctedP2Pens: Int? = null
    ) = withContext(Dispatchers.IO) {
        val match = matchDao.getMatchById(matchId) ?: return@withContext

        if (!approve) {
            matchDao.updateMatch(
                match.copy(
                    status = MatchStatus.SCHEDULED,
                    verificationStatus = "REJECTED"
                )
            )
            match.submittedByPlayerId?.let { sId ->
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = sId,
                        title = "Result Rejected by Admin",
                        message = "The submitted result for ${match.roundStage} was rejected. Please re-submit or contact admin.",
                        type = NotificationType.RESULT_REJECTED
                    )
                )
            }
            return@withContext
        }

        val finalP1Score = correctedP1Score ?: match.player1Score ?: 0
        val finalP2Score = correctedP2Score ?: match.player2Score ?: 0
        val finalP1Pens = correctedP1Pens ?: match.player1Penalties
        val finalP2Pens = correctedP2Pens ?: match.player2Penalties

        val winnerId = if (match.isKnockout) {
            when {
                finalP1Score > finalP2Score -> match.player1Id
                finalP2Score > finalP1Score -> match.player2Id
                (finalP1Pens ?: 0) > (finalP2Pens ?: 0) -> match.player1Id
                else -> match.player2Id
            }
        } else {
            when {
                finalP1Score > finalP2Score -> match.player1Id
                finalP2Score > finalP1Score -> match.player2Id
                else -> null // Draw
            }
        }

        val updatedMatch = match.copy(
            player1Score = finalP1Score,
            player2Score = finalP2Score,
            player1Penalties = finalP1Pens,
            player2Penalties = finalP2Pens,
            status = MatchStatus.VERIFIED,
            verificationStatus = "APPROVED",
            winnerId = winnerId
        )
        matchDao.updateMatch(updatedMatch)

        // 1. Update Player Stats in User Table
        updatePlayerStats(match.player1Id, finalP1Score, finalP2Score)
        updatePlayerStats(match.player2Id, finalP2Score, finalP1Score)

        // 2. Update Group Standings if Tournament Group match
        if (match.tournamentId != null && match.groupName != null) {
            updateGroupStanding(match.tournamentId, match.groupName, match.player1Id, match.player1Name, match.player1Team, finalP1Score, finalP2Score)
            updateGroupStanding(match.tournamentId, match.groupName, match.player2Id, match.player2Name, match.player2Team, finalP2Score, finalP1Score)
        }

        // 3. If Knockout Match, advance winner to next bracket node
        if (match.isKnockout && match.tournamentId != null && match.nextBracketNodeId != null) {
            val winnerName = if (winnerId == match.player1Id) match.player1Name else match.player2Name
            val winnerTeam = if (winnerId == match.player1Id) match.player1Team else match.player2Team

            val nextMatch = matchDao.getMatchByBracketNode(match.tournamentId, match.nextBracketNodeId)
            if (nextMatch != null && winnerId != null) {
                val updatedNextMatch = if (match.nextBracketSlot == 1) {
                    nextMatch.copy(
                        player1Id = winnerId,
                        player1Name = winnerName,
                        player1Team = winnerTeam
                    )
                } else {
                    nextMatch.copy(
                        player2Id = winnerId,
                        player2Name = winnerName,
                        player2Team = winnerTeam
                    )
                }
                matchDao.updateMatch(updatedNextMatch)
            }
        }

        // 4. If Final Match, declare Champion
        if (match.isKnockout && match.bracketNodeId == "FINAL" && match.tournamentId != null && winnerId != null) {
            val champName = if (winnerId == match.player1Id) match.player1Name else match.player2Name
            val runnerUpName = if (winnerId == match.player1Id) match.player2Name else match.player1Name
            val tourn = tournamentDao.getTournamentByIdOnce(match.tournamentId)
            if (tourn != null) {
                tournamentDao.updateTournament(
                    tourn.copy(
                        status = TournamentStatus.COMPLETED,
                        championName = champName,
                        runnerUpName = runnerUpName
                    )
                )
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = "ALL",
                        title = "🏆 Tournament Champion Crowned!",
                        message = "$champName has won the ${tourn.name}! Congratulations to the new BEFCC Champion!",
                        type = NotificationType.KNOCKOUT_ADVANCED
                    )
                )
            }
        }

        // Notify Players
        val notifMsg = "Result for ${match.roundStage} (${match.player1Name} $finalP1Score - $finalP2Score ${match.player2Name}) is verified."
        listOf(match.player1Id, match.player2Id).forEach { pid ->
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = pid,
                    title = "Match Result Approved",
                    message = notifMsg,
                    type = NotificationType.RESULT_APPROVED
                )
            )
        }
    }

    private suspend fun updatePlayerStats(playerId: String, gf: Int, ga: Int) {
        val user = userDao.getUserByIdOnce(playerId) ?: return
        val isWin = gf > ga
        val isDraw = gf == ga
        val isLoss = gf < ga

        val updated = user.copy(
            matchesPlayed = user.matchesPlayed + 1,
            wins = user.wins + (if (isWin) 1 else 0),
            draws = user.draws + (if (isDraw) 1 else 0),
            losses = user.losses + (if (isLoss) 1 else 0),
            goalsScored = user.goalsScored + gf,
            goalsConceded = user.goalsConceded + ga,
            points = user.points + (if (isWin) 3 else if (isDraw) 1 else 0)
        )
        userDao.updateUser(updated)
    }

    private suspend fun updateGroupStanding(
        tournamentId: String,
        groupName: String,
        playerId: String,
        playerName: String,
        playerTeam: String,
        gf: Int,
        ga: Int
    ) {
        val existing = standingDao.getStandingForPlayer(tournamentId, playerId)
            ?: StandingEntity(
                id = "${tournamentId}_${groupName}_$playerId",
                tournamentId = tournamentId,
                groupName = groupName,
                playerId = playerId,
                playerName = playerName,
                playerTeam = playerTeam
            )

        val isWin = gf > ga
        val isDraw = gf == ga
        val isLoss = gf < ga

        val updated = existing.copy(
            played = existing.played + 1,
            won = existing.won + (if (isWin) 1 else 0),
            drawn = existing.drawn + (if (isDraw) 1 else 0),
            lost = existing.lost + (if (isLoss) 1 else 0),
            goalsFor = existing.goalsFor + gf,
            goalsAgainst = existing.goalsAgainst + ga,
            goalDifference = (existing.goalsFor + gf) - (existing.goalsAgainst + ga),
            points = existing.points + (if (isWin) 3 else if (isDraw) 1 else 0)
        )
        standingDao.insertStanding(updated)

        // Recalculate group positions
        val groupStandings = standingDao.getStandingsByGroupOnce(tournamentId, groupName)
        val sorted = groupStandings.sortedWith(
            compareByDescending<StandingEntity> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
        )
        val ranked = sorted.mapIndexed { idx, s ->
            s.copy(position = idx + 1, isQualified = idx < 2)
        }
        standingDao.insertStandings(ranked)
    }

    // --- 1v1 Match Creation ---
    suspend fun create1v1Match(
        opponentId: String,
        opponentName: String,
        userTeam: String,
        opponentTeam: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in"))
        val match = MatchEntity(
            id = "match_1v1_${UUID.randomUUID().toString().take(8)}",
            tournamentId = null,
            tournamentName = "Independent 1v1 Match",
            matchType = MatchType.ONE_VS_ONE,
            roundStage = "1v1 Friendly Exhibition",
            player1Id = user.id,
            player1Name = user.fullName,
            player1Team = userTeam,
            player2Id = opponentId,
            player2Name = opponentName,
            player2Team = opponentTeam,
            scheduledTime = "2026-08-25 21:00",
            status = MatchStatus.SCHEDULED,
            verificationStatus = "NOT_SUBMITTED"
        )
        matchDao.insertMatch(match)

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = opponentId,
                title = "New 1v1 Challenge Received!",
                message = "${user.fullName} challenged you to a 1v1 match ($userTeam vs $opponentTeam).",
                type = NotificationType.MATCH_ASSIGNED
            )
        )

        Result.success(Unit)
    }

    suspend fun markNotificationsRead(userId: String) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    // --- Role & Player Authorization & Management ---
    suspend fun updateUserRole(targetUserId: String, newRole: UserRole): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in."))
        if (!current.isAdminOrLeader) {
            return@withContext Result.failure(Exception("Access denied. Admin privileges required."))
        }
        // Protect Super Admin / Leader Maruf Hossain
        if (targetUserId == InitialData.leaderUser.id) {
            return@withContext Result.failure(Exception("Super Admin / Leader (Maruf Hossain) cannot be modified or demoted."))
        }
        // Only Super Admin can promote someone to SUPER_ADMIN
        if (newRole == UserRole.SUPER_ADMIN && !current.isSuperAdmin) {
            return@withContext Result.failure(Exception("Only the Super Admin / Leader can assign Super Admin role."))
        }
        val target = userDao.getUserByIdOnce(targetUserId) ?: return@withContext Result.failure(Exception("User not found."))
        userDao.updateUser(target.copy(role = newRole))
        Result.success(Unit)
    }

    suspend fun deleteUser(targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in."))
        if (!current.isAdminOrLeader) {
            return@withContext Result.failure(Exception("Access denied. Admin privileges required."))
        }
        // Protect Leader Maruf Hossain
        if (targetUserId == InitialData.leaderUser.id) {
            return@withContext Result.failure(Exception("Super Admin / Leader (Maruf Hossain) cannot be deleted."))
        }
        val target = userDao.getUserByIdOnce(targetUserId) ?: return@withContext Result.failure(Exception("User not found."))
        if (target.isAdminOrLeader && !current.isSuperAdmin) {
            return@withContext Result.failure(Exception("Only Super Admin / Leader can delete Administrator accounts."))
        }
        userDao.deleteUser(targetUserId)
        Result.success(Unit)
    }

    // --- Admin System Settings (Dynamic Configuration) ---
    fun getOfficialContactNumberFlow(): Flow<SystemSettingEntity?> =
        systemSettingDao.getSettingFlow(SystemSettingEntity.KEY_OFFICIAL_CONTACT_NUMBER)

    fun getAllSettingsFlow(): Flow<List<SystemSettingEntity>> =
        systemSettingDao.getAllSettingsFlow()

    suspend fun updateOfficialContactNumber(newNumber: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in."))
        if (!current.isAdminOrLeader) {
            return@withContext Result.failure(Exception("Access denied. Only Super Admin or authorized Admins can change settings."))
        }
        val trimmed = newNumber.trim()
        val setting = SystemSettingEntity(
            key = SystemSettingEntity.KEY_OFFICIAL_CONTACT_NUMBER,
            value = trimmed,
            description = "Official BEFCC Merchant / Personal Contact Number for Player Registrations",
            updatedAt = System.currentTimeMillis()
        )
        systemSettingDao.insertOrUpdateSetting(setting)
        Result.success(Unit)
    }
}
