package com.vayu.agenticbrowser.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.vayu.agenticbrowser.ui.theme.*
import com.vayu.agenticbrowser.vault.AccountProfile
import com.vayu.agenticbrowser.vault.BiometricAuth
import com.vayu.agenticbrowser.vault.CryptoUtils
import com.vayu.agenticbrowser.vault.ProfileManager
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onBack: () -> Unit
) {
    val profileManager = remember { ProfileManager.getInstance() }
    val biometricAuth = remember { BiometricAuth.getInstance() }

    var profiles by remember { mutableStateOf(profileManager.listProfiles()) }
    var isUnlocked by remember { mutableStateOf(biometricAuth.requireUnlock()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = VayuCyan)
                        Text(
                            "VAYU Vault",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = VayuCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VayuSurfaceDark
                )
            )
        },
        floatingActionButton = {
            if (isUnlocked) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = VayuCyan,
                    contentColor = VayuNavy
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Profile")
                }
            }
        },
        containerColor = VayuSurfaceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Biometric unlock banner
            if (!isUnlocked) {
                Surface(
                    color = VayuError.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = VayuError
                        )
                        Text(
                            "Vault is locked. Tap to unlock with biometric.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VayuError
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Profile list
            if (profiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No profiles saved yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VayuOnSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        ProfileCard(
                            profile = profile,
                            onDelete = {
                                profileManager.deleteProfile(profile.id)
                                profiles = profileManager.listProfiles()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Profile Dialog
    if (showAddDialog) {
        AddProfileDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, siteUrl, username, password, phone, totpSeed ->
                val encryptedPassword = CryptoUtils.encrypt(password, "vayu_vault_key")
                val encryptedTotp = totpSeed?.takeIf { it.isNotBlank() }?.let {
                    CryptoUtils.encrypt(it, "vayu_vault_key")
                }
                val now = System.currentTimeMillis()
                val profile = AccountProfile(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    siteUrl = siteUrl,
                    username = username,
                    encryptedPassword = encryptedPassword,
                    phoneNumber = phone.takeIf { it.isNotBlank() },
                    encryptedTotpSeed = encryptedTotp,
                    createdAt = now,
                    lastUsedAt = now
                )
                profileManager.saveProfile(profile)
                profiles = profileManager.listProfiles()
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: AccountProfile,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = VayuSurfaceCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = VayuOnSurface
                )
                Text(
                    text = profile.siteUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = VayuCyan
                )
                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VayuOnSurfaceVariant
                )
            }

            TextButton(onClick = onDelete) {
                Text("Delete", color = VayuError)
            }
        }
    }
}

@Composable
private fun AddProfileDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, siteUrl: String, username: String, password: String, phone: String, totpSeed: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var siteUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var totpSeed by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VayuCyan,
                        cursorColor = VayuCyan
                    )
                )
                OutlinedTextField(
                    value = siteUrl,
                    onValueChange = { siteUrl = it },
                    label = { Text("Site URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VayuCyan,
                        cursorColor = VayuCyan
                    )
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username / Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VayuCyan,
                        cursorColor = VayuCyan
                    )
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VayuCyan,
                        cursorColor = VayuCyan
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VayuCyan,
                        cursorColor = VayuCyan
                    )
                )
                OutlinedTextField(
                    value = totpSeed,
                    onValueChange = { totpSeed = it },
                    label = { Text("TOTP Seed (Base32, optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VayuCyan,
                        cursorColor = VayuCyan
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && siteUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                        onSave(name, siteUrl, username, password, phone, totpSeed)
                    }
                },
                enabled = name.isNotBlank() && siteUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ) {
                Text("Save", color = VayuCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VayuOnSurfaceVariant)
            }
        }
    )
}
