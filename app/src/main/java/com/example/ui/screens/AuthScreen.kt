package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentUser
import com.example.model.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: SportsOpsViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val allUsers by viewModel.allUsers.collectAsState()
    val syncSummary by viewModel.cloudSyncSummary.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Quick Role, 1: Custom/Member Login, 2: Institutional

    // Custom form fields
    var customName by remember { mutableStateOf("Sports Coordinator") }
    var customEmail by remember { mutableStateOf("23f2000792@ds.study.iitm.ac.in") }
    var customDepartment by remember { mutableStateOf("Event Operations & Management") }
    var customPhone by remember { mutableStateOf("+91 98765 43210") }
    var selectedRole by remember { mutableStateOf(UserRole.CORE) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SportsNavyDark,
                        SportsNavyMedium,
                        SportsNavyDark
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Hero Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = SportsAmberPrimary.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SportsScore,
                        contentDescription = "App Logo",
                        tint = SportsAmberPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Inter-College Sports Operations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Command Center & Live Field Operations Platform",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cloud backend live indicator pill
            Surface(
                color = SportsNavySlate.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (syncSummary.status.isOnline) Color(0xFF4ADE80) else SportsAmberLight)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Firebase Firestore • ${syncSummary.status.label}",
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Tab Selection
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Team Roles", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_team_roles")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Custom Login", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_custom_login")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Institutional", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_institutional")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // Quick Persona / Role Selection
                        Text(
                            text = "Select your Committee Role to launch operations:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Instant access with pre-configured authority & permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(allUsers) { user ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.login(user)
                                            Toast.makeText(context, "Signed in as ${user.name} (${user.role.displayName})", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        }
                                        .testTag("user_role_item_${user.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            UserAvatar(name = user.name, colorHex = user.avatarColor, size = 38)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = user.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${user.vertical} • ${user.email}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        RoleBadge(role = user.role)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Custom Team Member / Account Setup
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Sign in or Register as a New Team Coordinator",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_name"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = customEmail,
                                onValueChange = { customEmail = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_email"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = customDepartment,
                                onValueChange = { customDepartment = it },
                                label = { Text("Department / Operational Vertical") },
                                leadingIcon = { Icon(Icons.Default.CorporateFare, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = customPhone,
                                onValueChange = { customPhone = it },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Role Selection
                            Text(
                                text = "Operational Hierarchy Level:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                UserRole.values().forEach { role ->
                                    val isSelected = selectedRole == role
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedRole = role },
                                        label = { Text(role.displayName, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    if (customName.isBlank() || customEmail.isBlank()) {
                                        Toast.makeText(context, "Please enter your name and email", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.loginWithCustomProfile(
                                        name = customName,
                                        email = customEmail,
                                        role = selectedRole,
                                        vertical = customDepartment,
                                        phone = customPhone
                                    )
                                    Toast.makeText(context, "Welcome, $customName!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_custom_signin"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SportsNavyDark)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch Session with Custom Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    2 -> {
                        // Institutional / IIT Madras Sign In
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Institutional Login",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Institutional Account Login",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Sign in directly with your verified institutional Google account (e.g. IIT Madras @study.iitm.ac.in)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // One-tap sign in for the user's specific email
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Active Verified Account:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "23f2000792@ds.study.iitm.ac.in",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "IIT Madras Sports Organizing Committee",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.loginWithCustomProfile(
                                        name = "IITM Sports Lead",
                                        email = "23f2000792@ds.study.iitm.ac.in",
                                        role = UserRole.CORE,
                                        vertical = "Organizing Directorate",
                                        phone = "+91 98765 43210"
                                    )
                                    Toast.makeText(context, "Signed in via IITM Institutional Account!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(horizontal = 8.dp)
                                    .testTag("btn_institutional_signin"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SportsNavyDark)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In with Institutional ID", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
