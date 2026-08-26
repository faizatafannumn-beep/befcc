package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.BefccRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BefccViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BefccRepository(AppDatabase.getDatabase(application), viewModelScope)

    val isInitialized: StateFlow<Boolean> = repository.isInitialized
    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.getAllTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchEntity>> = repository.getAllMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSlots: StateFlow<List<SlotEntity>> = repository.getAllSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMatches: StateFlow<List<MatchEntity>> = repository.getPendingMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications reactively for current user
    val userNotifications: StateFlow<List<NotificationEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else if (user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN) repository.getNotificationsForAdmin()
            else repository.getNotificationsForUser(user.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic System Settings
    val officialContactNumberSetting: StateFlow<SystemSettingEntity?> = repository.getOfficialContactNumberFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSystemSettings: StateFlow<List<SystemSettingEntity>> = repository.getAllSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Message state
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String) {
        _uiMessage.value = msg
    }

    // --- Authentication Actions ---
    fun login(identifier: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.login(identifier, pass)
            _isSubmitting.value = false
            result.onSuccess {
                _uiMessage.value = "Welcome back, ${it.fullName}!"
                onSuccess()
            }.onFailure {
                _uiMessage.value = it.message ?: "Login failed"
            }
        }
    }

    fun register(
        fullName: String,
        username: String,
        email: String,
        inGameUsername: String,
        favoriteTeam: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.registerUser(
                fullName, username, email, inGameUsername, favoriteTeam
            )
            _isSubmitting.value = false
            result.onSuccess {
                _uiMessage.value = "Account created! Player ID: ${it.playerId}"
                onSuccess()
            }.onFailure {
                _uiMessage.value = it.message ?: "Registration failed"
            }
        }
    }

    fun continueWithGoogle(name: String, email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.continueWithGoogle(name, email)
            _isSubmitting.value = false
            result.onSuccess {
                _uiMessage.value = "Signed in as ${it.fullName}"
                onSuccess()
            }.onFailure {
                _uiMessage.value = it.message ?: "Google sign in failed"
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiMessage.value = "Logged out successfully."
    }

    fun switchUserRole(toAdmin: Boolean) {
        viewModelScope.launch {
            repository.switchRole(toAdmin)
            _uiMessage.value = if (toAdmin) "Switched to Administrator role" else "Switched to Player role"
        }
    }

    fun updateProfile(
        fullName: String,
        inGameUsername: String,
        favoriteTeam: String,
        divisionRank: String,
        selectedTeams: String
    ) {
        viewModelScope.launch {
            repository.updateProfile(fullName, inGameUsername, favoriteTeam, divisionRank, selectedTeams)
            _uiMessage.value = "Profile updated successfully!"
        }
    }

    // --- Tournament Observables ---
    fun getSlotsForTournament(tournId: String): Flow<List<SlotEntity>> {
        return repository.getSlotsForTournament(tournId)
    }

    fun getStandingsForTournament(tournId: String): Flow<List<StandingEntity>> {
        return repository.getStandingsForTournament(tournId)
    }

    fun getMatchesForTournament(tournId: String): Flow<List<MatchEntity>> {
        return repository.getMatchesForTournament(tournId)
    }

    fun getMyMatches(): Flow<List<MatchEntity>> {
        val user = currentUser.value ?: return flowOf(emptyList())
        return repository.getMatchesForPlayer(user.id)
    }

    fun getMySlots(): Flow<List<SlotEntity>> {
        val user = currentUser.value ?: return flowOf(emptyList())
        return repository.getSlotsForPlayer(user.id)
    }

    // --- Slot Booking Action ---
    fun bookTournamentSlot(
        tournamentId: String,
        slotNumber: Int,
        selectedTeam: String,
        teamType: String,
        entryFee: Double,
        paymentMethod: String,
        transactionNumber: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.bookSlot(
                tournamentId, slotNumber, selectedTeam, teamType, entryFee, paymentMethod, transactionNumber
            )
            _isSubmitting.value = false
            result.onSuccess {
                _uiMessage.value = "Slot #$slotNumber requested! Awaiting Admin verification."
                onSuccess()
            }.onFailure {
                _uiMessage.value = it.message ?: "Booking failed"
            }
        }
    }

    // --- Match Result Submission ---
    fun submitMatchResult(
        matchId: String,
        p1Score: Int,
        p2Score: Int,
        p1Pens: Int? = null,
        p2Pens: Int? = null,
        notes: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.submitMatchResult(matchId, p1Score, p2Score, p1Pens, p2Pens, notes)
            _isSubmitting.value = false
            result.onSuccess {
                _uiMessage.value = "Result submitted for Admin verification."
                onSuccess()
            }.onFailure {
                _uiMessage.value = it.message ?: "Submission failed"
            }
        }
    }

    // --- 1v1 Challenge ---
    fun create1v1Challenge(
        opponentId: String,
        opponentName: String,
        userTeam: String,
        opponentTeam: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.create1v1Match(opponentId, opponentName, userTeam, opponentTeam)
            _isSubmitting.value = false
            result.onSuccess {
                _uiMessage.value = "1v1 Match created against $opponentName!"
                onSuccess()
            }.onFailure {
                _uiMessage.value = it.message ?: "Could not create 1v1 match"
            }
        }
    }

    // --- Admin Operations ---
    fun createTournament(
        name: String,
        type: String,
        playerLimit: Int,
        entryFee: Double,
        prizePool: String,
        teamType: TournamentTeamType,
        availableTeams: String,
        rules: String,
        startDate: String,
        endDate: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.createTournament(
                name, type, playerLimit, entryFee, prizePool, teamType, availableTeams, rules, startDate, endDate
            )
            _isSubmitting.value = false
            _uiMessage.value = "Tournament '$name' created successfully!"
            onSuccess()
        }
    }

    fun verifySlot(slotId: String, approve: Boolean, notes: String? = null) {
        viewModelScope.launch {
            repository.verifySlot(slotId, approve, notes)
            _uiMessage.value = if (approve) "Slot verified and confirmed!" else "Slot rejected."
        }
    }

    fun verifyMatchResult(
        matchId: String,
        approve: Boolean,
        p1Score: Int? = null,
        p2Score: Int? = null,
        p1Pens: Int? = null,
        p2Pens: Int? = null
    ) {
        viewModelScope.launch {
            repository.verifyMatchResult(matchId, approve, p1Score, p2Score, p1Pens, p2Pens)
            _uiMessage.value = if (approve) "Match result approved & standings updated!" else "Match result rejected."
        }
    }

    fun generateGroupsAndFixtures(tournamentId: String) {
        viewModelScope.launch {
            repository.generateGroupsAndFixtures(tournamentId)
            _uiMessage.value = "Groups and match fixtures generated!"
        }
    }

    fun generateKnockoutBracket(tournamentId: String) {
        viewModelScope.launch {
            repository.generateKnockoutBracket(tournamentId)
            _uiMessage.value = "Knockout stage & bracket generated!"
        }
    }

    fun updateTournamentStatus(tournamentId: String, status: TournamentStatus) {
        viewModelScope.launch {
            repository.updateTournamentStatus(tournamentId, status)
            _uiMessage.value = "Tournament status updated to ${status.name}"
        }
    }

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            repository.deleteTournament(tournamentId)
            _uiMessage.value = "Tournament deleted."
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            val result = repository.updateUserRole(userId, newRole)
            result.onSuccess {
                _uiMessage.value = "User role updated to ${newRole.name}."
            }.onFailure {
                _uiMessage.value = it.message ?: "Failed to update role."
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val result = repository.deleteUser(userId)
            result.onSuccess {
                _uiMessage.value = "User removed from system."
            }.onFailure {
                _uiMessage.value = it.message ?: "Failed to delete user."
            }
        }
    }

    fun markNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markNotificationsRead(user.id)
        }
    }

    // --- Dynamic System Settings Actions ---
    fun updateOfficialContactNumber(newNumber: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.updateOfficialContactNumber(newNumber)
            result.onSuccess {
                _uiMessage.value = "Official contact number updated successfully."
                onSuccess()
            }.onFailure {
                _uiMessage.value = it.message ?: "Failed to update official contact number."
            }
        }
    }
}
