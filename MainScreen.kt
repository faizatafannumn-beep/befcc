package com.example.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MatchEntity
import com.example.data.model.TournamentEntity
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.components.BefccLogo
import com.example.ui.components.BefccTopAppBar
import com.example.ui.components.SubmitResultDialog
import com.example.ui.matches.MatchesScreen
import com.example.ui.notifications.NotificationsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.slots.MySlotsScreen
import com.example.ui.standings.StandingsScreen
import com.example.ui.theme.*
import com.example.ui.tournaments.TournamentDetailScreen
import com.example.ui.tournaments.TournamentsScreen
import com.example.ui.viewmodel.BefccViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Tournaments : Screen("tournaments", "Tournaments", Icons.Outlined.EmojiEvents, Icons.Filled.EmojiEvents)
    object Matches : Screen("matches", "Matches", Icons.Outlined.SportsSoccer, Icons.Filled.SportsSoccer)
    object Standings : Screen("standings", "Standings", Icons.Outlined.Leaderboard, Icons.Filled.Leaderboard)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
}

sealed class SubScreen {
    object None : SubScreen()
    data class TournamentDetail(val tournamentId: String) : SubScreen()
    object AdminDashboard : SubScreen()
    object Notifications : SubScreen()
    object MySlots : SubScreen()
}

@Composable
fun MainScreen(
    viewModel: BefccViewModel,
    modifier: Modifier = Modifier
) {
    val isInitialized by viewModel.isInitialized.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val unreadNotifCount = remember(notifications) { notifications.count { !it.isRead } }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf<Screen>(Screen.Home) }
    var currentSubScreen by remember { mutableStateOf<SubScreen>(SubScreen.None) }

    // Dialog state for score submission
    var matchForScoreSubmission by remember { mutableStateOf<MatchEntity?>(null) }

    // Show snackbars when uiMessage updates
    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    if (!isInitialized) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BefccBackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                BefccLogo(size = 96.dp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "BEFCC",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = BefccNeonLime
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bangladesh eFootball Competitive Community",
                    style = MaterialTheme.typography.bodySmall,
                    color = BefccTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = BefccNeonLime,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        return
    }

    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                currentTab = Screen.Home
                currentSubScreen = SubScreen.None
            }
        )
        return
    }

    val user = currentUser!!

    Scaffold(
        topBar = {
            if (currentSubScreen == SubScreen.None) {
                BefccTopAppBar(
                    title = "BEFCC",
                    subtitle = "Bangladesh eFootball Competitive Community",
                    role = user.role,
                    unreadNotifCount = unreadNotifCount,
                    onNotifClick = { currentSubScreen = SubScreen.Notifications },
                    onAdminDashboardClick = {
                        if (user.isAdminOrLeader) {
                            currentSubScreen = SubScreen.AdminDashboard
                        }
                    },
                    onProfileClick = { currentTab = Screen.Profile }
                )
            }
        },
        bottomBar = {
            if (currentSubScreen == SubScreen.None) {
                Surface(
                    color = BefccSurfaceDark,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = BefccBorderDark.copy(alpha = 0.6f))
                ) {
                    NavigationBar(
                        containerColor = BefccSurfaceDark,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .testTag("bottom_navigation_bar")
                    ) {
                        val navItems = listOf(
                            Screen.Home,
                            Screen.Tournaments,
                            Screen.Matches,
                            Screen.Standings,
                            Screen.Profile
                        )

                        navItems.forEach { screen ->
                            val isSelected = currentTab == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentTab = screen
                                    currentSubScreen = SubScreen.None
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) BefccNeonLime else BefccTextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            letterSpacing = 0.8.sp,
                                            fontSize = 9.sp
                                        ),
                                        color = if (isSelected) BefccNeonLime else BefccTextMuted
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BefccNeonLime.copy(alpha = 0.15f),
                                    selectedIconColor = BefccNeonLime,
                                    unselectedIconColor = BefccTextMuted,
                                    selectedTextColor = BefccNeonLime,
                                    unselectedTextColor = BefccTextMuted
                                )
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BefccBackgroundDark,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val sub = currentSubScreen) {
                is SubScreen.TournamentDetail -> {
                    TournamentDetailScreen(
                        tournamentId = sub.tournamentId,
                        viewModel = viewModel,
                        onBack = { currentSubScreen = SubScreen.None },
                        onSubmitMatchScore = { match -> matchForScoreSubmission = match },
                        onAdminVerifyMatch = { match ->
                            viewModel.verifyMatchResult(match.id, approve = true)
                        }
                    )
                }

                is SubScreen.AdminDashboard -> {
                    if (user.isAdminOrLeader) {
                        AdminDashboardScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.None }
                        )
                    } else {
                        // Unauthorized access prevention
                        LaunchedEffect(Unit) {
                            currentSubScreen = SubScreen.None
                        }
                    }
                }

                is SubScreen.Notifications -> {
                    NotificationsScreen(
                        viewModel = viewModel
                    )
                }

                is SubScreen.MySlots -> {
                    MySlotsScreen(
                        viewModel = viewModel
                    )
                }

                SubScreen.None -> {
                    Crossfade(targetState = currentTab, label = "tab_crossfade") { screen ->
                        when (screen) {
                            Screen.Home -> {
                                UserHomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToTournament = { tourn ->
                                        currentSubScreen = SubScreen.TournamentDetail(tourn.id)
                                    },
                                    onNavigateToMatches = { currentTab = Screen.Matches },
                                    onNavigateToSlots = { currentSubScreen = SubScreen.MySlots },
                                    onNavigateToProfile = { currentTab = Screen.Profile },
                                    onOpenAdminPortal = { currentSubScreen = SubScreen.AdminDashboard },
                                    onSubmitMatchScore = { match -> matchForScoreSubmission = match }
                                )
                            }

                            Screen.Tournaments -> {
                                TournamentsScreen(
                                    viewModel = viewModel,
                                    onTournamentClick = { tourn ->
                                        currentSubScreen = SubScreen.TournamentDetail(tourn.id)
                                    }
                                )
                            }

                            Screen.Matches -> {
                                MatchesScreen(
                                    viewModel = viewModel,
                                    onSubmitScore = { match -> matchForScoreSubmission = match },
                                    onAdminVerifyScore = { match ->
                                        viewModel.verifyMatchResult(match.id, approve = true)
                                    }
                                )
                            }

                            Screen.Standings -> {
                                StandingsScreen(
                                    viewModel = viewModel
                                )
                            }

                            Screen.Profile -> {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onOpenAdminDashboard = {
                                        if (user.isAdminOrLeader) {
                                            currentSubScreen = SubScreen.AdminDashboard
                                        }
                                    },
                                    onLogout = {
                                        currentTab = Screen.Home
                                        currentSubScreen = SubScreen.None
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Submit Match Score Dialog
    matchForScoreSubmission?.let { match ->
        SubmitResultDialog(
            match = match,
            onDismiss = { matchForScoreSubmission = null },
            onSubmit = { p1Score, p2Score, p1Pens, p2Pens, notes ->
                viewModel.submitMatchResult(
                    matchId = match.id,
                    p1Score = p1Score,
                    p2Score = p2Score,
                    p1Pens = p1Pens,
                    p2Pens = p2Pens,
                    notes = notes,
                    onSuccess = { matchForScoreSubmission = null }
                )
            }
        )
    }
}
