package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PublicProfile
import com.example.ui.components.AvatarCircle
import com.example.ui.components.VerifiedTick
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobRed
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaPureBlack
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import com.example.util.ProfileCodec

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublicUserProfileSheet(
    profile: PublicProfile,
    isOwnProfile: Boolean,
    isBlocked: Boolean = false,
    onDismiss: () -> Unit,
    onMessageUser: () -> Unit = {},
    onEditMyProfile: () -> Unit = {},
    onToggleBlock: () -> Unit = {},
    onReportUser: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WaCard,
        modifier = Modifier.testTag("public_user_profile_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOwnProfile) "Your Public Profile Preview" else "Member Profile",
                    color = WaGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp).testTag("public_profile_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Profile", tint = WaTextPrimary)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvatarCircle(
                    name = profile.displayName,
                    photoUri = profile.photoUri,
                    size = 96.dp,
                    showOnlineDot = profile.isOnline,
                    vipTier = profile.vipTier,
                    badgeEmoji = profile.countryFlag
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = profile.displayName.ifBlank { "PGLOB Member" },
                        color = WaTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    VerifiedTick(profile.usernameVerified, isGold = profile.vipTier == "GOLD" || profile.vipTier == "DIAMOND")
                }
                Text(
                    text = "@${profile.username.ifBlank { "member" }}",
                    color = PglobGold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = buildString {
                                if (profile.city.isNotBlank()) append("${profile.city}, ")
                                append(profile.country.ifBlank { "Global" })
                            },
                            color = WaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(profile.language.ifBlank { "English" }, color = WaTextSecondary, fontSize = 13.sp)
                    }
                }
                if (profile.vipTier != "FREE") {
                    Surface(
                        color = PglobGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, PglobGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = PglobGold, modifier = Modifier.size(14.dp))
                            Text("${profile.vipTier} VIP", color = PglobGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = WaDivider)

            // Bio Card
            Surface(
                color = WaBackground,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, WaDivider, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Bio", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = profile.bio.ifBlank { profile.about.ifBlank { "Hey there! I am using PGLOB." } },
                        color = WaTextPrimary,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.testTag("public_profile_bio_text")
                    )
                }
            }

            // Interests Card
            Surface(
                color = WaBackground,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, WaDivider, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Interests (${profile.interests.size})",
                        color = WaGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (profile.interests.isEmpty()) {
                        Text(
                            text = "No interests listed yet.",
                            color = WaTextSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().testTag("public_profile_interests_flow"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            profile.interests.forEach { tag ->
                                Surface(
                                    color = WaGreen.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.border(1.dp, WaGreen.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                                ) {
                                    Text(
                                        text = tag,
                                        color = WaTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            if (isOwnProfile) {
                Button(
                    onClick = {
                        onDismiss()
                        onEditMyProfile()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("public_profile_edit_own_btn")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Bio, Photo & Interests", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                Button(
                    onClick = {
                        onDismiss()
                        onMessageUser()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("public_profile_message_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Message ${profile.displayName.substringBefore(" ")}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReportUser,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Report, contentDescription = null, tint = PglobGold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Report", color = PglobGold, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onToggleBlock,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = PglobRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isBlocked) "Unblock" else "Block", color = PglobRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileEditorSheet(
    initialName: String,
    initialAbout: String,
    initialBio: String,
    initialInterests: List<String>,
    initialPhotoUri: String,
    initialCountryLabel: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onPickPhotoGallery: () -> Unit,
    onPickPhotoCamera: () -> Unit,
    onRemovePhoto: () -> Unit,
    onPickCountry: () -> Unit,
    onSave: (name: String, about: String, bio: String, interests: List<String>, phone: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fullName by remember(initialName) { mutableStateOf(initialName) }
    var about by remember(initialAbout) { mutableStateOf(initialAbout) }
    var bio by remember(initialBio) { mutableStateOf(ProfileCodec.sanitizeBio(initialBio)) }
    var interests by remember(initialInterests) { mutableStateOf(initialInterests.take(ProfileCodec.MAX_INTERESTS_COUNT)) }
    var customInterestInput by remember { mutableStateOf("") }
    var phone by remember(initialPhone) { mutableStateOf(initialPhone) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PglobDarkGrey,
        modifier = Modifier.testTag("user_profile_editor_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Your PGLOB Profile",
                    color = WaTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp).testTag("profile_editor_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Editor", tint = WaTextPrimary)
                }
            }

            // Profile Picture Section
            Surface(
                color = WaBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, WaDivider, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AvatarCircle(
                            name = fullName.ifBlank { "Me" },
                            photoUri = initialPhotoUri,
                            size = 88.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(WaGreen)
                                .clickable { onPickPhotoGallery() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Upload photo", tint = WaPureBlack, modifier = Modifier.size(16.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onPickPhotoGallery,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("profile_editor_gallery_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WaGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Upload Photo", color = WaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onPickPhotoCamera,
                            modifier = Modifier.weight(1f).height(48.dp).testTag("profile_editor_camera_btn")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PglobGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Camera", color = PglobGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (initialPhotoUri.isNotBlank()) {
                        TextButton(onClick = onRemovePhoto, modifier = Modifier.testTag("profile_editor_remove_photo_btn")) {
                            Text("Remove Profile Picture", color = PglobRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it.take(80) },
                label = { Text("Display Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WaGreen,
                    unfocusedBorderColor = WaDivider,
                    focusedTextColor = WaTextPrimary,
                    unfocusedTextColor = WaTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("profile_editor_name_input")
            )

            OutlinedTextField(
                value = about,
                onValueChange = { about = it.take(200) },
                label = { Text("Quick Status Line") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WaGreen,
                    unfocusedBorderColor = WaDivider,
                    focusedTextColor = WaTextPrimary,
                    unfocusedTextColor = WaTextPrimary
                ),
                modifier = Modifier.fillMaxWidth().testTag("profile_editor_about_input")
            )

            // Short Bio with live counter
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Short Bio (Visible to Others)", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = ProfileCodec.formatBioCounter(bio),
                        color = WaTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag("profile_editor_bio_counter")
                    )
                }
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = ProfileCodec.sanitizeBio(it) },
                    placeholder = { Text("Tell other PGLOB members about yourself, your work, or vibes...") },
                    minLines = 3,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaGreen,
                        unfocusedBorderColor = WaDivider,
                        focusedTextColor = WaTextPrimary,
                        unfocusedTextColor = WaTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_editor_bio_input")
                )
            }

            // Interests Picker + Custom Tag Input
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_editor_interests_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Interests (${interests.size}/${ProfileCodec.MAX_INTERESTS_COUNT})",
                        color = WaGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text("Visible to other users", color = WaTextSecondary, fontSize = 12.sp)
                }

                if (interests.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("profile_editor_selected_interests")
                    ) {
                        interests.forEach { tag ->
                            Surface(
                                color = WaGreen,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.clickable {
                                    interests = ProfileCodec.removeInterest(interests, tag)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(tag, color = WaPureBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Icon(Icons.Default.Close, contentDescription = "Remove $tag", tint = WaPureBlack, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customInterestInput,
                        onValueChange = { customInterestInput = it.take(ProfileCodec.MAX_INTEREST_LENGTH) },
                        placeholder = { Text("Add custom interest (e.g. Comedy)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WaGreen,
                            unfocusedBorderColor = WaDivider,
                            focusedTextColor = WaTextPrimary,
                            unfocusedTextColor = WaTextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("profile_editor_interest_input")
                    )
                    Button(
                        onClick = {
                            interests = ProfileCodec.addInterest(interests, customInterestInput)
                            customInterestInput = ""
                        },
                        enabled = customInterestInput.trim().isNotBlank() && interests.size < ProfileCodec.MAX_INTERESTS_COUNT,
                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp).testTag("profile_editor_add_interest_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Interest")
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }

                Text("Tap to pick popular interests:", color = WaTextSecondary, fontSize = 12.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileCodec.SUGGESTED_INTERESTS.forEach { suggestion ->
                        val isSelected = interests.any { it.equals(suggestion, ignoreCase = true) }
                        Surface(
                            color = if (isSelected) WaGreen.copy(alpha = 0.25f) else WaInput,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .border(1.dp, if (isSelected) WaGreen else WaDivider, RoundedCornerShape(16.dp))
                                .clickable {
                                    interests = if (isSelected) {
                                        ProfileCodec.removeInterest(interests, suggestion)
                                    } else {
                                        ProfileCodec.addInterest(interests, suggestion)
                                    }
                                }
                                .testTag("suggested_interest_${suggestion.lowercase().replace(Regex("[^a-z0-9]"), "_")}")
                        ) {
                            Text(
                                text = if (isSelected) "✓ $suggestion" else "+ $suggestion",
                                color = if (isSelected) WaGreen else WaTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.take(30) },
                label = { Text("Phone Number") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WaGreen,
                    unfocusedBorderColor = WaDivider,
                    focusedTextColor = WaTextPrimary,
                    unfocusedTextColor = WaTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = onPickCountry,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(initialCountryLabel, color = WaTextPrimary, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = {
                    val pendingInterests = if (customInterestInput.trim().isNotBlank()) {
                        ProfileCodec.addInterest(interests, customInterestInput)
                    } else {
                        interests
                    }
                    onSave(fullName, about, bio, pendingInterests, phone)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaPureBlack),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("profile_editor_save_btn")
            ) {
                Text("Save Profile", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}
