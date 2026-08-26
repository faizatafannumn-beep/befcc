package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BefccLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.BefccViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: BefccViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("maruf@befcc.org") }
    var loginPassword by remember { mutableStateOf("admin123") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register Form State
    var regFullName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regInGameUsername by remember { mutableStateOf("") }
    var regFavoriteTeam by remember { mutableStateOf("Bangladesh") }

    val isSubmitting by viewModel.isSubmitting.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        BefccBackgroundDark,
                        Color(0xFF0F241A),
                        BefccBackgroundDark
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // BEFCC Official Logo
            BefccLogo(size = 96.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bangladesh eFootball\nCompetitive Community",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = BefccTextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "BEFCC OFFICIAL TOURNAMENT PORTAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = BefccNeonLime,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BefccSurfaceGlass.copy(alpha = 0.85f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(BefccNeonLime.copy(alpha = 0.7f), BefccNeonCyan.copy(alpha = 0.4f)))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Mode Selector Tabs (Login / Create Account)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BefccSurfaceDark)
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isRegisterMode = false },
                            color = if (!isRegisterMode) BefccNeonLime else Color.Transparent
                        ) {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = if (!isRegisterMode) BefccTextDarkOnNeon else BefccTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isRegisterMode = true },
                            color = if (isRegisterMode) BefccNeonLime else Color.Transparent
                        ) {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = if (isRegisterMode) BefccTextDarkOnNeon else BefccTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isRegisterMode) {
                        // --- LOGIN FORM ---
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text("Email or Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BefccNeonLime) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BefccNeonLime,
                                unfocusedBorderColor = BefccBorderDark,
                                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BefccNeonLime) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = BefccTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BefccNeonLime,
                                unfocusedBorderColor = BefccBorderDark,
                                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.login(loginIdentifier, loginPassword, onAuthSuccess)
                            },
                            enabled = loginIdentifier.isNotBlank() && !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BefccNeonLime,
                                contentColor = BefccTextDarkOnNeon
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = BefccTextDarkOnNeon, modifier = Modifier.size(22.dp))
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null, tint = BefccTextDarkOnNeon)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Login to BEFCC",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = BefccTextDarkOnNeon
                                )
                            }
                        }

                        // Quick Test Accounts Bar
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Quick Sign-In (Official Leadership):",
                            style = MaterialTheme.typography.labelSmall,
                            color = BefccTextMuted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    loginIdentifier = "maruf@befcc.org"
                                    loginPassword = "admin123"
                                    viewModel.login("maruf@befcc.org", "admin123", onAuthSuccess)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccGoldAccent),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(BefccGoldAccent.copy(alpha = 0.5f), BefccBorderDark))
                                )
                            ) {
                                Text("Leader (Maruf)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            OutlinedButton(
                                onClick = {
                                    loginIdentifier = "jabir@befcc.org"
                                    loginPassword = "admin123"
                                    viewModel.login("jabir@befcc.org", "admin123", onAuthSuccess)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BefccNeonCyan),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(BefccNeonCyan.copy(alpha = 0.5f), BefccBorderDark))
                                )
                            ) {
                                Text("Admin (Jabir)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    } else {
                        // --- CREATE ACCOUNT FORM ---
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = BefccNeonLime) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("register_fullname_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BefccNeonLime,
                                unfocusedBorderColor = BefccBorderDark,
                                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Gamer Username / Tag") },
                            leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = null, tint = BefccNeonLime) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("register_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BefccNeonLime,
                                unfocusedBorderColor = BefccBorderDark,
                                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BefccNeonLime) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BefccNeonLime,
                                unfocusedBorderColor = BefccBorderDark,
                                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regInGameUsername,
                            onValueChange = { regInGameUsername = it },
                            label = { Text("eFootball In-Game Name / ID") },
                            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = BefccNeonLime) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("register_ingame_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BefccNeonLime,
                                unfocusedBorderColor = BefccBorderDark,
                                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regFavoriteTeam,
                            onValueChange = { regFavoriteTeam = it },
                            label = { Text("Favorite Team") },
                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = BefccNeonLime) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("register_team_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BefccNeonLime,
                                unfocusedBorderColor = BefccBorderDark,
                                focusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                                unfocusedContainerColor = BefccSurfaceCardElevated.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.register(
                                    regFullName, regUsername, regEmail, regInGameUsername, regFavoriteTeam, onAuthSuccess
                                )
                            },
                            enabled = regFullName.isNotBlank() && regUsername.isNotBlank() && regEmail.isNotBlank() && !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BefccNeonLime,
                                contentColor = BefccTextDarkOnNeon
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = BefccTextDarkOnNeon, modifier = Modifier.size(22.dp))
                            } else {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = BefccTextDarkOnNeon)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Create BEFCC Account",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = BefccTextDarkOnNeon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = BefccBorderSubtle.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google / Gmail Sign In Button
                    OutlinedButton(
                        onClick = { showGoogleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BefccSurfaceCardElevated.copy(alpha = 0.5f),
                            contentColor = BefccTextPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(BefccBorderSubtle, BefccBorderDark))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Logo",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google / Gmail",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = BefccTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Official Bangladesh eFootball Competitive Platform\nStrict Fair Play & Verified Results",
                style = MaterialTheme.typography.labelSmall,
                color = BefccTextMuted,
                textAlign = TextAlign.Center
            )
        }

        // Google Sign-In One-Tap Simulation Dialog
        if (showGoogleDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleDialog = false },
                containerColor = BefccSurfaceDark,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BefccLogo(size = 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continue with Google", color = BefccTextPrimary, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                text = {
                    Column {
                        Text(
                            "Select your Gmail account to sign into BEFCC:",
                            color = BefccTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Account Option 1
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showGoogleDialog = false
                                    viewModel.continueWithGoogle("Faizata Fannum", "faizatafannumn@gmail.com", onAuthSuccess)
                                }
                                .padding(8.dp),
                            color = BefccSurfaceCard
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(BefccEmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("FF", color = BefccBackgroundDark, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Faizata Fannum", color = BefccTextPrimary, fontWeight = FontWeight.Bold)
                                    Text("faizatafannumn@gmail.com", color = BefccTextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Account Option 2
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showGoogleDialog = false
                                    viewModel.continueWithGoogle("Fahim Ahmed", "fahim.ahmed@gmail.com", onAuthSuccess)
                                }
                                .padding(8.dp),
                            color = BefccSurfaceCard
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(BefccGoldAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("FA", color = BefccBackgroundDark, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Fahim Ahmed", color = BefccTextPrimary, fontWeight = FontWeight.Bold)
                                    Text("fahim.ahmed@gmail.com", color = BefccTextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGoogleDialog = false }) {
                        Text("Cancel", color = BefccTextMuted)
                    }
                }
            )
        }
    }
}
