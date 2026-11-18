package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DatingMatch
import com.example.data.model.LiveRoom
import com.example.data.model.PublicProfile
import com.example.data.model.UserPrivateProfile
import com.example.ui.AiUiState
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.PglobAiAssistantSheet
import com.example.ui.components.VerifiedTick
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobRed
import com.example.ui.theme.PglobWhite

@Composable
fun DatingScreen(
    myProfile: UserPrivateProfile?,
    publicProfiles: List<PublicProfile>,
    matches: List<DatingMatch>,
    blockedIds: Set<String>,
    viewModel: PglobViewModel,
    onOpenChatById: (String) -> Unit
) {
    var subTab by remember { mutableStateOf("Discover") } // Discover, Matches, Likes
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterCountry by remember { mutableStateOf("All") }
    var filterOnlineOnly by remember { mutableStateOf(false) }
    var matchedPopupProfile by remember { mutableStateOf<PublicProfile?>(null) }

    // 18+ Setup state if dating not yet enabled
    var ageStr by remember { mutableStateOf("24") }
    var gender by remember { mutableStateOf("Male") }
    var lookingFor by remember { mutableStateOf("Female") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Lagos") }
    var goal by remember { mutableStateOf("Serious") }

    val seedCandidates = remember(publicProfiles, blockedIds, filterCountry, filterOnlineOnly, myProfile?.userId) {
        val base = publicProfiles.filter { it.userId != myProfile?.userId && !blockedIds.contains(it.userId) }
        val pool = if (base.isNotEmpty()) base else listOf(
            PublicProfile(userId = "date_1", displayName = "Zara Bello", username = "zara_b", country = "Nigeria", countryFlag = "🇳🇬", city = "Lagos", age = 24, bio = "Music, Business & Sunset walks 🌍", relationshipGoal = "Serious", usernameVerified = true, isOnline = true),
            PublicProfile(userId = "date_2", displayName = "Chloe Vance", username = "chloe_uk", country = "United Kingdom", countryFlag = "🇬🇧", city = "London", age = 26, bio = "Travel, Coffee & Tech Founder ✨", relationshipGoal = "Dating", usernameVerified = true, isOnline = true),
            PublicProfile(userId = "date_3", displayName = "Nadia Silva", username = "nadia_br", country = "Brazil", countryFlag = "🇧🇷", city = "Rio", age = 23, bio = "Dancing, Cooking & Ocean Vibes 💃", relationshipGoal = "Serious", usernameVerified = false, isOnline = false),
            PublicProfile(userId = "date_4", displayName = "Amina Mensah", username = "amina_gh", country = "Ghana", countryFlag = "🇬🇭", city = "Accra", age = 25, bio = "Church, Movies & Entrepreneurship ❤️", relationshipGoal = "Marriage", usernameVerified = true, isOnline = true)
        )
        pool.filter { p ->
            (filterCountry == "All" || p.country.equals(filterCountry, true)) &&
                (!filterOnlineOnly || p.isOnline)
        }
    }
    var cardIndex by remember { mutableIntStateOf(0) }

    if (myProfile?.datingEnabled != true) {
        // Setup Dating Profile (18+ verified)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Find Love • Dating Setup ❤️", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Text("18+ Verification Required • Safe & Private", color = PglobGreyText)
            OutlinedTextField(
                value = ageStr,
                onValueChange = { ageStr = it.filter(Char::isDigit).take(2) },
                label = { Text("Your Age (Must be 18+)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Male", "Female").forEach { g ->
                    Button(
                        onClick = { gender = g },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gender == g) PglobGold else PglobDarkGrey,
                            contentColor = if (gender == g) PglobBlack else PglobWhite
                        )
                    ) { Text("I am $g") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Male", "Female", "Both").forEach { lf ->
                    Button(
                        onClick = { lookingFor = lf },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lookingFor == lf) PglobGold else PglobDarkGrey,
                            contentColor = if (lookingFor == lf) PglobBlack else PglobWhite
                        )
                    ) { Text(lf, fontSize = 12.sp) }
                }
            }
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio & Interests (Music, Football, Movies...)") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Friendship", "Dating", "Serious", "Marriage").forEach { g ->
                    Surface(
                        color = if (goal == g) PglobGold else PglobDarkGrey,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).clickable { goal = g }
                    ) {
                        Text(g, color = if (goal == g) PglobBlack else PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                }
            }
            Button(
                onClick = {
                    val age = ageStr.toIntOrNull() ?: 0
                    viewModel.enableDatingProfile(age, gender, lookingFor, bio, city, goal)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
            ) {
                Text("Turn ON Dating ❤️", fontWeight = FontWeight.ExtraBold)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Discover", "Matches (${matches.size})", "Likes").forEach { tab ->
                        val key = tab.substringBefore(" ")
                        Surface(
                            color = if (subTab == key) PglobGold else PglobDarkGrey,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { subTab = key }
                        ) {
                            Text(
                                tab,
                                color = if (subTab == key) PglobBlack else PglobWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.boostDatingProfile() }) {
                        Icon(Icons.Default.Bolt, contentDescription = "Boost 200 coins", tint = PglobGold)
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = PglobWhite)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            when (subTab) {
                "Discover" -> {
                    val candidate = seedCandidates.getOrNull(cardIndex % seedCandidates.size.coerceAtLeast(1))
                    if (candidate == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No profiles match your filter right now.", color = PglobGreyText)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xFF282008), PglobDarkGrey, PglobBlack)))
                                .border(1.5.dp, PglobGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Row(modifier = Modifier.align(Alignment.TopEnd), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = {
                                    viewModel.submitReport("DATING_USER", candidate.userId, "Safety report from Discover")
                                }) {
                                    Icon(Icons.Default.Report, contentDescription = "Report", tint = PglobGreyText)
                                }
                                TextButton(onClick = { viewModel.toggleBlockUser(candidate.userId); cardIndex++ }) {
                                    Text("Block", color = PglobRed, fontSize = 12.sp)
                                }
                            }

                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarCircle(name = candidate.displayName, photoUri = candidate.photoUri, size = 140.dp, showOnlineDot = candidate.isOnline, vipTier = "GOLD")
                                Spacer(Modifier.height(14.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${candidate.displayName}, ${candidate.age} ${candidate.countryFlag}",
                                        color = PglobWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    VerifiedTick(candidate.usernameVerified)
                                }
                                Text("${candidate.city}, ${candidate.country} • ${candidate.relationshipGoal}", color = PglobGold, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(10.dp))
                                Text(candidate.bio.ifBlank { candidate.about }, color = PglobWhite, style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = PglobDarkGrey,
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp).border(2.dp, PglobRed, CircleShape).clickable {
                                    viewModel.swipeDating(candidate, "PASS") {}
                                    cardIndex++
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Close, contentDescription = "Pass", tint = PglobRed, modifier = Modifier.size(32.dp))
                                }
                            }
                            Surface(
                                color = PglobDarkGrey,
                                shape = CircleShape,
                                modifier = Modifier.size(58.dp).border(2.dp, Color(0xFF34B7F1), CircleShape).clickable {
                                    viewModel.swipeDating(candidate, "SUPER_LIKE") { matchedPopupProfile = it }
                                    cardIndex++
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Star, contentDescription = "Super Like 50c", tint = Color(0xFF34B7F1), modifier = Modifier.size(28.dp))
                                }
                            }
                            Surface(
                                color = PglobGold,
                                shape = CircleShape,
                                modifier = Modifier.size(68.dp).clickable {
                                    viewModel.swipeDating(candidate, "LIKE") { matchedPopupProfile = it }
                                    cardIndex++
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Like", tint = PglobBlack, modifier = Modifier.size(34.dp))
                                }
                            }
                        }
                    }
                }

                "Matches" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(matches, key = { it.id }) { m ->
                            Surface(color = PglobDarkGrey, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Match #${m.id.takeLast(5)} ❤️", color = PglobWhite, fontWeight = FontWeight.Bold)
                                        Text("Tap Chat Now to message", color = PglobGreyText, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { onOpenChatById(m.chatId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                                    ) { Text("Chat Now") }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(90.dp)) }
                    }
                }

                else -> {
                    val isGold = myProfile.vipTier == "GOLD" || myProfile.vipTier == "DIAMOND"
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!isGold) {
                            Text("Upgrade to GOLD VIP to see who liked you!", color = PglobGold, fontWeight = FontWeight.Bold)
                        }
                        seedCandidates.take(3).forEach { liker ->
                            Surface(
                                color = PglobDarkGrey,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().then(if (!isGold) Modifier.blur(6.dp) else Modifier)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AvatarCircle(name = liker.displayName, size = 44.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text("${liker.displayName} ${liker.countryFlag} liked you ❤️", color = PglobWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (matchedPopupProfile != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                ConfettiOverlay(visible = true)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("IT'S A MATCH! 🎉", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                    Spacer(Modifier.height(14.dp))
                    AvatarCircle(name = matchedPopupProfile!!.displayName, size = 110.dp, vipTier = "GOLD")
                    Spacer(Modifier.height(12.dp))
                    Text("You and ${matchedPopupProfile!!.displayName} liked each other!", color = PglobWhite)
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            val target = matchedPopupProfile!!
                            matchedPopupProfile = null
                            viewModel.openOrCreateChatWith("${target.displayName} ${target.countryFlag}", target.userId, false, target.countryFlag) { cid ->
                                onOpenChatById(cid)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Chat Now", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { matchedPopupProfile = null }) {
                        Text("Keep Swiping", color = PglobWhite)
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            containerColor = PglobDarkGrey,
            title = { Text("Dating Filters", color = PglobGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Nigeria", "United Kingdom", "Ghana", "Brazil").forEach { c ->
                            Surface(
                                color = if (filterCountry == c) PglobGold else PglobBlack,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable { filterCountry = c }
                            ) {
                                Text(c, color = if (filterCountry == c) PglobBlack else PglobWhite, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    TextButton(onClick = { filterOnlineOnly = !filterOnlineOnly }) {
                        Text(if (filterOnlineOnly) "Online Now: ON ✔️" else "Online Now: OFF", color = PglobGold)
                    }
                }
            },
            confirmButton = { Button(onClick = { showFilterSheet = false }) { Text("Apply") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectGlobalScreen(
    publicProfiles: List<PublicProfile>,
    currentUserId: String,
    viewModel: PglobViewModel,
    onOpenChatId: (String) -> Unit
) {
    var selectedCountry by remember { mutableStateOf("All") }
    var inspectedProfile by remember { mutableStateOf<PublicProfile?>(null) }

    val pool = remember(publicProfiles, selectedCountry, currentUserId) {
        val base = publicProfiles.filter { it.userId != currentUserId }
        val list = if (base.isNotEmpty()) base else listOf(
            PublicProfile(userId = "amina_ng", displayName = "Amina Yusuf", username = "amina_lagos", country = "Nigeria", countryFlag = "🇳🇬", city = "Lagos", language = "English", bio = "Lagos community builder running Ajo circles.", interests = listOf("Ajo Savings", "Business", "Fashion"), isOnline = true),
            PublicProfile(userId = "marcus_us", displayName = "Marcus Vance", username = "marcus_nyc", country = "USA", countryFlag = "🇺🇸", city = "New York", language = "English", bio = "Hosting weekly global audio rooms.", interests = listOf("Music", "Travel", "Photography"), isOnline = true),
            PublicProfile(userId = "sofia_br", displayName = "Sofia Costa", username = "sofia_br", country = "Brazil", countryFlag = "🇧🇷", city = "Rio", language = "Portuguese", bio = "Ocean vibes & PGLOB creator.", interests = listOf("Travel", "Food & Cooking", "Movies"), isOnline = true),
            PublicProfile(userId = "priya_in", displayName = "Priya Sharma", username = "priya_tech", country = "India", countryFlag = "🇮🇳", city = "Bengaluru", language = "Hindi", bio = "Product designer exploring global tech.", interests = listOf("Design", "Tech & AI", "Reading"), isOnline = false)
        )
        if (selectedCountry == "All") list else list.filter { it.country.equals(selectedCountry, true) }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Connect Globally 🌍", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Button(
                onClick = {
                    val randomPeer = pool.randomOrNull()
                    if (randomPeer != null) {
                        viewModel.openOrCreateChatWith("${randomPeer.displayName} ${randomPeer.countryFlag}", randomPeer.userId, false, randomPeer.countryFlag, onOpenChatId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
            ) { Text("Random Chat") }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Nigeria", "USA", "United States", "Brazil", "India", "Ghana", "United Kingdom").forEach { c ->
                Surface(
                    color = if (selectedCountry == c) PglobGold else PglobDarkGrey,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { selectedCountry = c }
                ) {
                    Text(c, color = if (selectedCountry == c) PglobBlack else PglobWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(pool, key = { it.userId }) { p ->
                Surface(
                    color = PglobDarkGrey,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { inspectedProfile = p }
                        .testTag("connect_member_card_${p.userId}")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(name = p.displayName, photoUri = p.photoUri, size = 50.dp, showOnlineDot = p.isOnline, vipTier = p.vipTier, badgeEmoji = p.countryFlag)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(p.displayName, color = PglobWhite, fontWeight = FontWeight.Bold)
                                    VerifiedTick(p.usernameVerified, isGold = p.vipTier == "GOLD" || p.vipTier == "DIAMOND")
                                }
                                Text("@${p.username} • ${p.country} • ${p.language}", color = PglobGreyText, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    viewModel.openOrCreateChatWith("${p.displayName} ${p.countryFlag}", p.userId, false, p.countryFlag, onOpenChatId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                            ) { Text("Chat") }
                        }
                        val bioSnippet = p.bio.ifBlank { p.about }
                        if (bioSnippet.isNotBlank()) {
                            Text(
                                text = bioSnippet,
                                color = PglobWhite.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (p.interests.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                p.interests.take(4).forEach { tag ->
                                    Surface(
                                        color = PglobGreen.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.border(1.dp, PglobGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    ) {
                                        Text(
                                            text = tag,
                                            color = PglobWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }
    }

    if (inspectedProfile != null) {
        val target = inspectedProfile!!
        PublicUserProfileSheet(
            profile = target,
            isOwnProfile = target.userId == currentUserId,
            onDismiss = { inspectedProfile = null },
            onMessageUser = {
                viewModel.openOrCreateChatWith("${target.displayName} ${target.countryFlag}", target.userId, false, target.countryFlag, onOpenChatId)
            },
            onReportUser = {
                viewModel.submitReport("USER_PROFILE", target.userId, "Reported from Connect")
                inspectedProfile = null
            },
            onToggleBlock = {
                viewModel.toggleBlockUser(target.userId)
                inspectedProfile = null
            }
        )
    }
}

@Composable
fun LiveWorldRoomsScreen(
    liveRooms: List<LiveRoom>,
    aiState: AiUiState,
    viewModel: PglobViewModel
) {
    var mode by remember { mutableStateOf("AUDIO") } // AUDIO or VIDEO
    var showGoLive by remember { mutableStateOf(false) }
    var activeRoom by remember { mutableStateOf<LiveRoom?>(null) }
    var roomComment by remember { mutableStateOf("") }
    var commentsList by remember { mutableStateOf(listOf("Amina 🇳🇬: Fire vibes!", "Marcus 🇺🇸: Greetings from NYC!", "Kwame 🇬🇭: Drop the beat!")) }
    var showAiTranslate by remember { mutableStateOf(false) }

    val rooms = remember(liveRooms, mode) {
        val base = if (liveRooms.isNotEmpty()) liveRooms else listOf(
            LiveRoom(id = "r1", hostName = "DJ Kofi", title = "Afrobeats & Global Music Room", category = "Music", mode = "AUDIO", countryFlag = "🇬🇭", viewerCount = 248),
            LiveRoom(id = "r2", hostName = "Naija vs USA", title = "Nigeria vs USA Culture Debate", category = "Debate", mode = "AUDIO", countryFlag = "🇳🇬", viewerCount = 512),
            LiveRoom(id = "r3", hostName = "Coach Ada", title = "Earn Money & PGLOB Pay Secrets", category = "Finance", mode = "AUDIO", countryFlag = "🇳🇬", viewerCount = 180),
            LiveRoom(id = "r4", hostName = "Zara Live", title = "Global Love & Talent Stage", category = "Talent", mode = "VIDEO", countryFlag = "🇬🇧", viewerCount = 390)
        )
        base.filter { it.mode == mode }
    }

    if (activeRoom != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF1F1600), PglobBlack)))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${activeRoom!!.countryFlag} ${activeRoom!!.title}", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Host: ${activeRoom!!.hostName} • 🔴 LIVE (${activeRoom!!.viewerCount} watching)", color = PglobGreen, fontSize = 12.sp)
                }
                Button(
                    onClick = { activeRoom = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobRed, contentColor = PglobWhite)
                ) { Text("Leave") }
            }

            // Host & Speakers Grid
            Surface(color = PglobDarkGrey, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (activeRoom!!.mode == "VIDEO") Icons.Default.Videocam else Icons.Default.Mic,
                        contentDescription = null,
                        tint = PglobGold,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("${activeRoom!!.hostName} is Live (${activeRoom!!.mode})", color = PglobWhite, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { viewModel.showToast("Joined Speaker Stage 🎤") }) {
                            Text("Join Speaker", color = PglobGold)
                        }
                        OutlinedButton(onClick = {
                            val last = commentsList.lastOrNull() ?: "Hello World"
                            viewModel.askPglobAi("Translate this live room comment to English and French: $last")
                            showAiTranslate = true
                        }) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = PglobGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Translate", color = PglobGold)
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(commentsList) { c ->
                    Text(c, color = PglobWhite, fontSize = 13.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = roomComment,
                    onValueChange = { roomComment = it },
                    placeholder = { Text("Comment in live room...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (roomComment.isNotBlank()) {
                            commentsList = commentsList + "Me: $roomComment"
                            roomComment = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Send") }
                IconButton(onClick = { viewModel.showToast("❤️ Sent floating heart!") }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Heart", tint = PglobRed)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("AUDIO" to "Live Audio 🎙️", "VIDEO" to "Live Video 📹").forEach { (m, label) ->
                        Button(
                            onClick = { mode = m },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mode == m) PglobGold else PglobDarkGrey,
                                contentColor = if (mode == m) PglobBlack else PglobWhite
                            )
                        ) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                Button(
                    onClick = { showGoLive = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobRed, contentColor = PglobWhite)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Go Live")
                }
            }

            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(rooms) { room ->
                    Surface(
                        color = PglobDarkGrey,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().clickable { activeRoom = room }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(name = room.hostName, size = 48.dp, showOnlineDot = true, badgeEmoji = room.countryFlag)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(room.title, color = PglobWhite, fontWeight = FontWeight.Bold)
                                Text("${room.hostName} • ${room.category} • 🔴 ${room.viewerCount} live", color = PglobGold, fontSize = 12.sp)
                            }
                            Text("Join", color = PglobGreen, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }

    if (showGoLive) {
        var title by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Music") }
        AlertDialog(
            onDismissRequest = { showGoLive = false },
            containerColor = PglobDarkGrey,
            title = { Text("Start Live $mode Room", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Room Title") }, singleLine = true)
                    OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Category (Music, Debate, Finance...)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createLiveRoom(title, cat, mode, "🌍")
                            showGoLive = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Start Live") }
            },
            dismissButton = { TextButton(onClick = { showGoLive = false }) { Text("Cancel", color = PglobWhite) } }
        )
    }

    if (showAiTranslate) {
        PglobAiAssistantSheet(
            aiState = aiState,
            onAsk = { viewModel.askPglobAi(it) },
            onDismiss = { showAiTranslate = false; viewModel.clearAiState() }
        )
    }
}
