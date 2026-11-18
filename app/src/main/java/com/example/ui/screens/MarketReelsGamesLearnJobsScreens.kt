package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.LocalAppSettings
import com.example.data.model.GlobalCatalogData
import com.example.data.model.JobListing
import com.example.data.model.LearnCourse
import com.example.data.model.MarketProduct
import com.example.data.model.ReelPost
import com.example.data.model.UserPrivateProfile
import com.example.ui.MarketCheckoutOutcome
import com.example.ui.PglobViewModel
import com.example.ui.components.AvatarCircle
import com.example.ui.components.CountryPickerSheet
import com.example.ui.components.VerifiedTick
import com.example.ui.components.createTempCameraUri
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobBlueTick
import com.example.ui.theme.PglobDarkGrey
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobGreyText
import com.example.ui.theme.PglobRed
import com.example.ui.theme.PglobRoyalPurple
import com.example.ui.theme.PglobWhite
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGold
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaSolidGoldSurface
import com.example.ui.theme.WaSolidGreenSurface
import com.example.ui.theme.WaSolidRedSurface
import com.example.ui.theme.WaStickyBarBg
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

@Composable
fun MarketScreen(
    products: List<MarketProduct>,
    myProfile: UserPrivateProfile?,
    localSettings: LocalAppSettings = LocalAppSettings(),
    viewModel: PglobViewModel,
    onOpenChatId: (String) -> Unit,
    onOpenCheckout: ((MarketProduct) -> Unit)? = null,
    onOpenOrderHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showPostDialog by remember { mutableStateOf(false) }
    var buyTarget by remember { mutableStateOf<MarketProduct?>(null) }

    var newTitle by remember { mutableStateOf("") }
    var newCoins by remember { mutableStateOf("500") }
    var newCash by remember { mutableStateOf("5000") }
    var newCategory by remember { mutableStateOf("Phones") }
    var newDesc by remember { mutableStateOf("") }
    var newLoc by remember { mutableStateOf(myProfile?.country ?: "Nigeria") }
    var newImageUri by remember { mutableStateOf("") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) newImageUri = uri.toString() }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok && tempCameraUri != null) newImageUri = tempCameraUri.toString() }

    val camPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val u = createTempCameraUri(context)
            tempCameraUri = u
            cameraLauncher.launch(u)
        } else {
            viewModel.showToast("Allow Camera in Settings to photograph products")
        }
    }

    val categories = listOf("All", "Phones", "Fashion", "Electronics", "Services", "Vehicles", "Beauty")
    val seedProducts = remember {
        listOf(
            MarketProduct(
                id = "seed_prod_1",
                sellerId = "amina_ng",
                sellerName = "Amina Gadget Hub",
                sellerFlag = "🇳🇬",
                sellerVerified = true,
                title = "iPhone 15 Pro Max 256GB (Clean)",
                priceCoins = 1500,
                priceCash = 1250000.0,
                category = "Phones",
                description = "Factory unlocked, Face ID intact, worldwide shipping.",
                location = "Lagos, Nigeria"
            ),
            MarketProduct(
                id = "seed_prod_2",
                sellerId = "kwame_gh",
                sellerName = "Kwame Luxury Kente",
                sellerFlag = "🇬🇭",
                sellerVerified = true,
                title = "Royal Gold Handwoven Agbada Set",
                priceCoins = 450,
                priceCash = 85000.0,
                category = "Fashion",
                description = "Bespoke luxury gold embroidery, ready to wear.",
                location = "Accra, Ghana"
            ),
            MarketProduct(
                id = "seed_prod_3",
                sellerId = "marcus_us",
                sellerName = "Vance Studio Gear",
                sellerFlag = "🇺🇸",
                sellerVerified = true,
                title = "Creator Pro Mic + Ring Light Kit",
                priceCoins = 300,
                priceCash = 45000.0,
                category = "Electronics",
                description = "Perfect for PGLOB Live Video & Reels.",
                location = "New York, USA"
            )
        )
    }
    val pool = remember(products, seedProducts) {
        (products + seedProducts).distinctBy { it.id }
    }

    val filtered = remember(pool, searchQuery, selectedCategory) {
        pool.filter { p ->
            (selectedCategory == "All" || p.category.equals(selectedCategory, true)) &&
                (searchQuery.isBlank() || p.title.contains(searchQuery, true) || p.location.contains(searchQuery, true))
        }
    }

    val currencySymbol = myProfile?.currency?.ifBlank { "₦" } ?: "₦"
    val openSellerChat: (MarketProduct) -> Unit = { prod ->
        viewModel.openOrCreateChatWith(
            title = "${prod.sellerName} ${prod.sellerFlag}",
            targetUid = prod.sellerId,
            flag = prod.sellerFlag
        ) { chatId ->
            viewModel.sendChatMessage(
                chatId,
                listOf(viewModel.currentUserId, prod.sellerId),
                "Hi! Is \"${prod.title}\" still available on PGLOB Market?"
            )
            buyTarget = null
            onOpenChatId(chatId)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search PGLOB Market...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onOpenOrderHistory,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("market_open_orders_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Order History", tint = WaGold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Orders", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    Surface(
                        color = if (selectedCategory == cat) PglobGold else PglobDarkGrey,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            cat,
                            color = if (selectedCategory == cat) PglobBlack else PglobWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(filtered, key = { it.id }) { prod ->
                    val formattedCash = remember(prod.priceCash) {
                        NumberFormat.getNumberInstance(Locale.US).format(prod.priceCash.toLong())
                    }
                    val openProdCheckout: () -> Unit = {
                        if (onOpenCheckout != null) {
                            onOpenCheckout(prod)
                        } else {
                            buyTarget = prod
                        }
                    }
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WaDivider, RoundedCornerShape(16.dp))
                            .clickable(onClick = openProdCheckout)
                            .testTag("market_card_${prod.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProductVisualHero(product = prod, height = 190.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AvatarCircle(name = prod.sellerName, size = 32.dp)
                                    Text(
                                        "${prod.sellerFlag} ${prod.sellerName}",
                                        color = WaTextSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Visible
                                    )
                                    if (prod.sellerVerified) {
                                        VerifiedTick(true, isGold = true)
                                    }
                                }
                                Surface(color = WaBackground, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        prod.category,
                                        color = WaGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                prod.title,
                                color = WaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 23.sp
                            )
                            Text(
                                prod.description,
                                color = WaTextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Visible
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "$currencySymbol$formattedCash  •  🪙 ${prod.priceCoins} Coins",
                                    color = WaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    lineHeight = 25.sp
                                )
                                Text(
                                    prod.location,
                                    color = WaTextSecondary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = openProdCheckout,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("market_buy_btn_${prod.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = { openSellerChat(prod) },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .testTag("market_chat_btn_${prod.id}"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat Seller", tint = WaGold, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Chat", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                                }
                                IconButton(
                                    onClick = { viewModel.submitReport("PRODUCT", prod.id, "Reported listing ${prod.title}") },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Report, contentDescription = "Report Product", tint = PglobRed)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showPostDialog = true },
            containerColor = WaGold,
            contentColor = WaBackground,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("post_product_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }

        buyTarget?.let { prod ->
            MarketProductCheckoutOverlay(
                product = prod,
                currencySymbol = currencySymbol,
                walletBalance = myProfile?.walletBalance ?: localSettings.localWalletBalance,
                coinsBalance = myProfile?.coins ?: localSettings.localCoins,
                defaultLocation = myProfile?.country ?: prod.location,
                defaultCountry = myProfile?.country?.ifBlank { "Nigeria" } ?: "Nigeria",
                defaultPhone = myProfile?.phone?.ifBlank { localSettings.chattingPhoneNumber } ?: localSettings.chattingPhoneNumber,
                onDismiss = { buyTarget = null },
                onChatSeller = { openSellerChat(prod) },
                onViewOrders = {
                    buyTarget = null
                    onOpenOrderHistory()
                },
                onConfirmPurchase = { qty, deliveryOption, deliveryAddress, deliveryFeeCash, paymentMethod ->
                    viewModel.purchaseMarketProductWithEscrow(
                        product = prod,
                        quantity = qty,
                        deliveryOption = deliveryOption,
                        deliveryAddress = deliveryAddress,
                        deliveryFeeCash = deliveryFeeCash,
                        paymentMethod = paymentMethod
                    )
                }
            )
        }
    }

    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            containerColor = PglobDarkGrey,
            title = { Text("Sell on PGLOB Market", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Product Title") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCoins,
                            onValueChange = { newCoins = it.filter(Char::isDigit) },
                            label = { Text("Price Coins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newCash,
                            onValueChange = { newCash = it.filter(Char::isDigit) },
                            label = { Text("Price Cash") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        categories.drop(1).forEach { cat ->
                            Surface(
                                color = if (newCategory == cat) PglobGold else PglobBlack,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable { newCategory = cat }
                            ) {
                                Text(cat, color = if (newCategory == cat) PglobBlack else PglobWhite, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    OutlinedTextField(value = newLoc, onValueChange = { newLoc = it }, label = { Text("Location") }, singleLine = true)
                    OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text("Description") }, maxLines = 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    val u = createTempCameraUri(context)
                                    tempCameraUri = u
                                    cameraLauncher.launch(u)
                                } else {
                                    camPermLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Camera", fontSize = 12.sp)
                        }
                    }
                    if (newImageUri.isNotBlank()) {
                        Text("✓ Photo attached", color = PglobGreen, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.postProduct(
                                title = newTitle.trim(),
                                priceCoins = newCoins.toIntOrNull() ?: 100,
                                priceCash = newCash.toDoubleOrNull() ?: 1000.0,
                                category = newCategory,
                                description = newDesc.ifBlank { "Available on PGLOB Market" },
                                location = newLoc.ifBlank { "Global" },
                                imageUri = newImageUri
                            )
                            newTitle = ""; newDesc = ""; newImageUri = ""
                            showPostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Post Item") }
            },
            dismissButton = {
                TextButton(onClick = { showPostDialog = false }) { Text("Cancel", color = PglobWhite) }
            }
        )
    }
}

@Composable
fun ProductVisualHero(
    product: MarketProduct,
    height: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bundledDrawableRes = remember(product.id, product.title) {
        when {
            product.id == "seed_prod_1" || product.title.contains("iPhone", ignoreCase = true) ->
                R.drawable.img_market_iphone_15_pro_1794858969468
            product.id == "seed_prod_2" || product.title.contains("Agbada", ignoreCase = true) ->
                R.drawable.img_market_royal_agbada_1794858969471
            product.id == "seed_prod_3" || product.title.contains("Mic", ignoreCase = true) ->
                R.drawable.img_market_creator_mic_1794858969811
            else -> null
        }
    }
    val fallbackIcon = when {
        product.category.equals("Phones", true) -> Icons.Default.Smartphone
        product.category.equals("Fashion", true) -> Icons.Default.Checkroom
        product.category.equals("Electronics", true) -> Icons.Default.Mic
        else -> Icons.Default.ShoppingBag
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(WaBackground)
            .border(1.dp, WaDivider, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            product.imageUri.isNotBlank() -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(product.imageUri))
                        .size(1024, 1024)
                        .crossfade(false)
                        .build(),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            bundledDrawableRes != null -> {
                Image(
                    painter = painterResource(id = bundledDrawableRes),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(WaSolidGoldSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = product.title,
                            tint = WaGold,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Text(
                        text = product.title,
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Surface(
            color = WaSolidGreenSurface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .border(1.dp, WaGreen, RoundedCornerShape(8.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = WaGreen, modifier = Modifier.size(15.dp))
                Text("PGLOB Escrow", color = WaGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class DeliveryOptionSpec(
    val label: String,
    val feeCash: Double,
    val eta: String
)

@Composable
fun MarketProductCheckoutOverlay(
    product: MarketProduct,
    currencySymbol: String,
    walletBalance: Double,
    coinsBalance: Long,
    defaultLocation: String,
    defaultCountry: String = "Nigeria",
    defaultPhone: String = "",
    onDismiss: () -> Unit,
    onChatSeller: () -> Unit,
    onAddMoney: () -> Unit = {},
    onViewOrders: () -> Unit = onDismiss,
    onConfirmPurchase: suspend (
        quantity: Int,
        deliveryOption: String,
        deliveryAddress: String,
        deliveryFeeCash: Double,
        paymentMethod: String
    ) -> MarketCheckoutOutcome
) {
    BackHandler { onDismiss() }
    val scope = rememberCoroutineScope()

    var quantity by remember(product.id) { mutableIntStateOf(1) }
    val deliveryOptions = remember {
        listOf(
            DeliveryOptionSpec("Express Doorstep Delivery", 2500.0, "1-2 Days"),
            DeliveryOptionSpec("Pickup / Meet Seller", 0.0, "Same Day"),
            DeliveryOptionSpec("Worldwide Insured Shipping", 12000.0, "3-5 Days")
        )
    }
    var selectedDelivery by remember(product.id) { mutableStateOf(deliveryOptions.first().label) }
    var selectedCountry by remember(product.id) { mutableStateOf(defaultCountry.ifBlank { "Nigeria" }) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var cityInput by remember(product.id) {
        val guessedCity = defaultLocation.substringBefore(",").trim()
            .takeIf { it.isNotBlank() && !it.equals(defaultCountry, ignoreCase = true) }
            ?: "Lagos"
        mutableStateOf(guessedCity)
    }
    var streetInput by remember(product.id) { mutableStateOf("") }
    var phoneInput by remember(product.id) { mutableStateOf(defaultPhone) }
    var selectedPaymentMethod by remember(product.id) { mutableStateOf("WALLET") }
    var isProcessing by remember(product.id) { mutableStateOf(false) }
    var outcome by remember(product.id) { mutableStateOf<MarketCheckoutOutcome>(MarketCheckoutOutcome.Idle) }

    val selectedSpec = deliveryOptions.firstOrNull { it.label == selectedDelivery } ?: deliveryOptions.first()
    val deliveryFeeCash = selectedSpec.feeCash
    val deliveryFeeCoins = if (deliveryFeeCash > 0.0) (deliveryFeeCash / 500.0).toInt().coerceAtLeast(5) else 0
    val unitCashFormatted = remember(product.priceCash) {
        NumberFormat.getNumberInstance(Locale.US).format(product.priceCash.toLong())
    }
    val itemsCash = product.priceCash * quantity
    val itemsCashFormatted = remember(itemsCash) {
        NumberFormat.getNumberInstance(Locale.US).format(itemsCash.toLong())
    }
    val totalCash = itemsCash + deliveryFeeCash
    val totalCoins = (product.priceCoins * quantity) + deliveryFeeCoins
    val totalCashFormatted = remember(totalCash) {
        NumberFormat.getNumberInstance(Locale.US).format(totalCash.toLong())
    }
    val deliveryFeeFormatted = remember(deliveryFeeCash) {
        NumberFormat.getNumberInstance(Locale.US).format(deliveryFeeCash.toLong())
    }
    val walletFormatted = remember(walletBalance) {
        NumberFormat.getNumberInstance(Locale.US).format(walletBalance.toLong())
    }
    val totalDisplay = if (selectedPaymentMethod == "COINS") {
        "🪙 $totalCoins Coins"
    } else {
        "$currencySymbol$totalCashFormatted"
    }
    val isInsufficientBalance = when (selectedPaymentMethod) {
        "WALLET" -> walletBalance < totalCash
        "COINS" -> coinsBalance < totalCoins
        else -> false
    }

    val executeConfirmPay: () -> Unit = {
        if (!isProcessing) {
            val composedAddress = if (selectedDelivery == "Pickup / Meet Seller") {
                "Pickup with ${product.sellerName} ($selectedCountry)"
            } else {
                listOf(
                    streetInput.trim(),
                    cityInput.trim(),
                    selectedCountry.trim(),
                    phoneInput.trim().takeIf { it.isNotBlank() }?.let { "Tel: $it" }
                ).filterNotNull().filter { it.isNotBlank() }.joinToString(", ")
            }
            isProcessing = true
            scope.launch {
                outcome = onConfirmPurchase(
                    quantity,
                    selectedDelivery,
                    composedAddress,
                    deliveryFeeCash,
                    selectedPaymentMethod
                )
                isProcessing = false
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = WaBackground,
        unfocusedContainerColor = WaBackground,
        focusedBorderColor = WaGreen,
        unfocusedBorderColor = WaDivider,
        focusedTextColor = WaTextPrimary,
        unfocusedTextColor = WaTextPrimary,
        focusedLabelColor = WaGreen,
        unfocusedLabelColor = WaTextSecondary,
        focusedPlaceholderColor = WaTextSecondary,
        unfocusedPlaceholderColor = WaTextSecondary
    )

    Surface(
        color = WaBackground,
        modifier = Modifier
            .fillMaxSize()
            .testTag("market_checkout_overlay")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Checkout Header (Solid Surface, Zero Blur)
            Surface(color = WaCard, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp).testTag("checkout_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WaTextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                            Text(
                                "Product Detail & Checkout",
                                color = WaTextPrimary,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                lineHeight = 25.sp
                            )
                            Text(
                                "PGLOB Escrow Protected • Fast Delivery",
                                color = WaGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 19.sp
                            )
                        }
                        Surface(
                            color = WaSolidGreenSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.border(1.dp, WaGreen, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                "Escrow Safe",
                                color = WaGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = WaDivider, thickness = 1.dp)
                }
            }

            // Full Scrollable Checkout Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProductVisualHero(product = product, height = 195.dp)

                // Product Info Card
                Surface(
                    color = WaCard,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WaDivider, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                AvatarCircle(name = product.sellerName, size = 38.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "${product.sellerFlag} ${product.sellerName}",
                                            color = WaGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            softWrap = true,
                                            overflow = TextOverflow.Visible
                                        )
                                        if (product.sellerVerified) {
                                            Spacer(Modifier.width(4.dp))
                                            VerifiedTick(true, isGold = true)
                                        }
                                    }
                                    Text(
                                        product.location,
                                        color = WaTextSecondary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        Text(
                            product.title,
                            color = WaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 27.sp
                        )
                        Text(
                            product.description,
                            color = WaTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            softWrap = true,
                            overflow = TextOverflow.Visible
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = WaBackground,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.border(1.dp, WaGreen, RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    "$currencySymbol$unitCashFormatted",
                                    color = WaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            Surface(
                                color = WaBackground,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.border(1.dp, WaGold, RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    "🪙 ${product.priceCoins} Coins",
                                    color = WaGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Success or Failure Outcome Banner
                when (val res = outcome) {
                    is MarketCheckoutOutcome.Success -> {
                        Surface(
                            color = WaSolidGreenSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, WaGreen, RoundedCornerShape(16.dp))
                                .testTag("checkout_order_success_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Order Placed", tint = WaGreen, modifier = Modifier.size(32.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "✅ Order Placed!",
                                            color = WaGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            lineHeight = 26.sp
                                        )
                                        Text(
                                            "Order No: ${res.orderNumber}",
                                            color = WaTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                    }
                                }
                                Text(
                                    "Money dey PGLOB Escrow 🔒",
                                    color = WaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                HorizontalDivider(color = WaGreen)
                                Text("Item: ${res.quantity}x ${res.productTitle}", color = WaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text("Seller: ${res.sellerName}", color = WaTextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text("Delivery: ${res.deliveryOption}", color = WaTextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text("Payment: ${res.paymentMethod} • ${res.paidSummary}", color = WaGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(res.escrowHoldSummary, color = WaTextPrimary, fontSize = 15.sp, lineHeight = 21.sp, softWrap = true)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = onViewOrders,
                                        modifier = Modifier.weight(1f).height(50.dp).testTag("checkout_view_orders_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("View My Orders", fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = onChatSeller,
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = WaGold, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Chat Seller", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    is MarketCheckoutOutcome.Failure -> {
                        Surface(
                            color = WaSolidRedSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, PglobRed, RoundedCornerShape(16.dp))
                                .testTag("checkout_order_error_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = "Payment Failed", tint = PglobRed, modifier = Modifier.size(26.dp))
                                    Text("Payment Could Not Complete", color = PglobRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Text(
                                    res.reason,
                                    color = WaTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 22.sp,
                                    softWrap = true,
                                    overflow = TextOverflow.Visible
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = executeConfirmPay,
                                        modifier = Modifier.weight(1f).height(48.dp).testTag("checkout_retry_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = WaGold, contentColor = WaBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Retry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    if (selectedPaymentMethod != "CASH") {
                                        OutlinedButton(
                                            onClick = {
                                                selectedPaymentMethod = "CASH"
                                                outcome = MarketCheckoutOutcome.Idle
                                            },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Cash on Delivery", color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MarketCheckoutOutcome.Idle -> {}
                }

                if (outcome !is MarketCheckoutOutcome.Success) {
                    // Quantity Selector
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, WaDivider, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quantity", color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                OutlinedButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.size(48.dp).testTag("checkout_qty_minus")
                                ) {
                                    Text("-", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Text(
                                    quantity.toString(),
                                    color = WaTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                OutlinedButton(
                                    onClick = { if (quantity < 20) quantity++ },
                                    modifier = Modifier.size(48.dp).testTag("checkout_qty_plus")
                                ) {
                                    Text("+", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    // Delivery Method & Full Delivery Address Card
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, WaDivider, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = WaGold, modifier = Modifier.size(22.dp))
                                Text("Delivery Option & Address", color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            deliveryOptions.forEach { spec ->
                                val selected = selectedDelivery == spec.label
                                val feeText = if (spec.feeCash == 0.0) {
                                    "Free"
                                } else {
                                    "$currencySymbol${NumberFormat.getNumberInstance(Locale.US).format(spec.feeCash.toLong())}"
                                }
                                Surface(
                                    color = if (selected) WaSolidGoldSurface else WaBackground,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .border(1.5.dp, if (selected) WaGold else WaDivider, RoundedCornerShape(12.dp))
                                        .clickable { selectedDelivery = spec.label }
                                        .testTag("checkout_delivery_${spec.label.replace(" ", "_")}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                            Text(
                                                spec.label,
                                                color = WaTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                softWrap = true,
                                                overflow = TextOverflow.Visible
                                            )
                                            Text(
                                                "Estimated: ${spec.eta}",
                                                color = WaTextSecondary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                softWrap = true
                                            )
                                        }
                                        Text(
                                            feeText,
                                            color = if (selected) WaGold else WaGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                            if (selectedDelivery == "Pickup / Meet Seller") {
                                Surface(
                                    color = WaSolidGreenSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, WaGreen, RoundedCornerShape(12.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            "You go meet seller for agreed place - Chat seller to agree place",
                                            color = WaTextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp,
                                            lineHeight = 22.sp,
                                            softWrap = true,
                                            overflow = TextOverflow.Visible
                                        )
                                        Button(
                                            onClick = onChatSeller,
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Chat Seller", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    color = WaBackground,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, WaDivider, RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = WaGreen, modifier = Modifier.size(20.dp))
                                                Text(
                                                    "Country: $selectedCountry",
                                                    color = WaTextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                            TextButton(
                                                onClick = { showCountryPicker = true },
                                                modifier = Modifier.testTag("checkout_change_country_btn")
                                            ) {
                                                Text("Change", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            }
                                        }
                                        Text(
                                            "Delivery Fee: ${if (deliveryFeeCash == 0.0) "Free" else "$currencySymbol$deliveryFeeFormatted"}  •  Estimated: ${selectedSpec.eta}",
                                            color = WaGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            softWrap = true
                                        )
                                        OutlinedTextField(
                                            value = cityInput,
                                            onValueChange = { cityInput = it },
                                            label = { Text("City", fontSize = 14.sp) },
                                            placeholder = { Text("Enter City e.g Lagos", fontSize = 15.sp) },
                                            singleLine = true,
                                            colors = textFieldColors,
                                            modifier = Modifier.fillMaxWidth().testTag("checkout_city_input")
                                        )
                                        OutlinedTextField(
                                            value = streetInput,
                                            onValueChange = { streetInput = it },
                                            label = { Text("Street Address", fontSize = 14.sp) },
                                            placeholder = { Text("Enter Street + House No", fontSize = 15.sp) },
                                            maxLines = 2,
                                            colors = textFieldColors,
                                            modifier = Modifier.fillMaxWidth().testTag("checkout_street_input")
                                        )
                                        OutlinedTextField(
                                            value = phoneInput,
                                            onValueChange = { phoneInput = it },
                                            label = { Text("Phone", fontSize = 14.sp) },
                                            placeholder = { Text("Enter Delivery Phone", fontSize = 15.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            singleLine = true,
                                            colors = textFieldColors,
                                            modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payment Method Selector (Auto Height Cards, Zero Cut Text)
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, WaDivider, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = WaGold, modifier = Modifier.size(22.dp))
                                Text("Payment Method", color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            listOf(
                                Triple("WALLET", "PGLOB Wallet ($currencySymbol)", "Balance: $currencySymbol$walletFormatted"),
                                Triple("COINS", "PGLOB Coins (🪙)", "Balance: $coinsBalance Coins"),
                                Triple("CASH", "Cash on Delivery", "Pay rider upon verified inspection")
                            ).forEach { (key, title, subtitle) ->
                                val selected = selectedPaymentMethod == key
                                Surface(
                                    color = if (selected) WaSolidGreenSurface else WaBackground,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .border(1.5.dp, if (selected) WaGreen else WaDivider, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedPaymentMethod = key
                                            outcome = MarketCheckoutOutcome.Idle
                                        }
                                        .testTag("checkout_pay_method_$key")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                title,
                                                color = WaTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                softWrap = true,
                                                maxLines = 2,
                                                overflow = TextOverflow.Visible
                                            )
                                            Text(
                                                subtitle,
                                                color = WaTextSecondary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp,
                                                softWrap = true,
                                                maxLines = 3,
                                                overflow = TextOverflow.Visible
                                            )
                                        }
                                        if (selected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = WaGreen, modifier = Modifier.size(26.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Order Summary Card (Always Fully Visible)
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, WaGold, RoundedCornerShape(16.dp))
                            .testTag("checkout_order_summary_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Order Summary", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Product Price ($quantity x)", color = WaTextSecondary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                Text(
                                    if (selectedPaymentMethod == "COINS") "${product.priceCoins * quantity} Coins"
                                    else "$currencySymbol$itemsCashFormatted",
                                    color = WaTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Delivery (${selectedSpec.eta})", color = WaTextSecondary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                Text(
                                    if (selectedPaymentMethod == "COINS") {
                                        if (deliveryFeeCoins == 0) "Free" else "$deliveryFeeCoins Coins"
                                    } else {
                                        if (deliveryFeeCash == 0.0) "Free" else "$currencySymbol$deliveryFeeFormatted"
                                    },
                                    color = WaTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PGLOB Escrow Protection", color = WaGreen, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                Text("Included 🔒", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            HorizontalDivider(color = WaDivider, thickness = 1.dp)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", color = WaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text(
                                    totalDisplay,
                                    color = WaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Sticky Bottom Confirm Bar (Always Visible Above Phone Home Indicator, Never Covered by Nav)
            Surface(
                color = WaStickyBarBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_sticky_bottom_bar")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = WaDivider, thickness = 1.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (outcome is MarketCheckoutOutcome.Success) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onChatSeller,
                                    modifier = Modifier.weight(1f).height(52.dp).testTag("checkout_chat_seller_btn"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat Seller", tint = WaGold, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Chat Seller", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                                }
                                Button(
                                    onClick = onViewOrders,
                                    modifier = Modifier.weight(1.2f).height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("View My Orders", fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                                }
                            }
                        } else {
                            if (isInsufficientBalance) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Balance No Reach - Add Money",
                                        color = WaGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = {
                                        selectedPaymentMethod = "CASH"
                                        outcome = MarketCheckoutOutcome.Idle
                                    }) {
                                        Text("Use Cash on Delivery", color = WaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(0.42f)) {
                                    Text(
                                        "Total",
                                        color = WaTextSecondary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        totalDisplay,
                                        color = WaGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                                OutlinedButton(
                                    onClick = onChatSeller,
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("checkout_chat_seller_btn"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat Seller", tint = WaGold, modifier = Modifier.size(20.dp))
                                }
                                if (isInsufficientBalance) {
                                    Button(
                                        onClick = {
                                            onAddMoney()
                                            if (selectedPaymentMethod == "WALLET") {
                                                outcome = MarketCheckoutOutcome.Failure(
                                                    reason = "Balance No Reach ($currencySymbol$walletFormatted). Tap Add Money in Pay Wallet or switch to Cash on Delivery.",
                                                    paymentMethod = "PGLOB Wallet"
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(0.58f)
                                            .height(52.dp)
                                            .testTag("checkout_add_money_btn"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WaGold, contentColor = WaBackground)
                                    ) {
                                        Text(
                                            "Add Money",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            maxLines = 1
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = executeConfirmPay,
                                        enabled = !isProcessing,
                                        modifier = Modifier
                                            .weight(0.58f)
                                            .height(52.dp)
                                            .testTag("checkout_confirm_pay_btn"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WaGreen, contentColor = WaBackground)
                                    ) {
                                        if (isProcessing) {
                                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = WaBackground, strokeWidth = 2.5.dp)
                                        } else {
                                            Text(
                                                "Confirm & Pay",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCountryPicker) {
        CountryPickerSheet(
            onDismiss = { showCountryPicker = false },
            onSelect = { info -> selectedCountry = info.name }
        )
    }
}

@Composable
fun ReelsScreen(
    reels: List<ReelPost>,
    myProfile: UserPrivateProfile?,
    viewModel: PglobViewModel
) {
    val context = LocalContext.current
    var showUpload by remember { mutableStateOf(false) }
    var caption by remember { mutableStateOf("") }
    var hashtag by remember { mutableStateOf("#pglobworld") }
    var music by remember { mutableStateOf("Afrobeats Gold Mix") }
    var commentReelTarget by remember { mutableStateOf<ReelPost?>(null) }
    var commentInput by remember { mutableStateOf("") }
    val followedAuthors = remember { mutableStateMapOf<String, Boolean>() }
    val reelComments = remember { mutableStateMapOf<String, List<String>>() }

    val pool = remember(reels) {
        if (reels.isNotEmpty()) reels else listOf(
            ReelPost("reel_seed_1", "amina_ng", "Amina Yusuf 🇳🇬", "Loving the clean PGLOB Black UI + Ajo Escrow on PGLOB APP! 💚✨", "#pglobapp", "Lagos Sunset Beat", 124, 1890),
            ReelPost("reel_seed_2", "marcus_us", "Marcus Vance 🇺🇸", "Just earned 500 coins on the Daily Spin Wheel! 🪙🔥", "#earncoins", "Luxury Trap Instrumental", 89, 940),
            ReelPost("reel_seed_3", "kwame_gh", "Kwame Mensah 🇬🇭", "Live Music Room tonight — join from anywhere!", "#globallive", "Accra Highlife Remix", 215, 3420)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PGLOB Reels 🎬", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("+10 coins per upload", color = PglobGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            items(pool, key = { it.id }) { reel ->
                val isFollowing = followedAuthors[reel.authorId] == true
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF1F1600), PglobRoyalPurple.copy(alpha = 0.6f), PglobBlack)))
                        .border(1.dp, PglobGold.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(PglobGold.copy(alpha = 0.2f))
                            .border(2.dp, PglobGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Reel", tint = PglobGold, modifier = Modifier.size(36.dp))
                    }

                    // Bottom Info
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(0.78f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AvatarCircle(name = reel.authorName, size = 36.dp, showOnlineDot = true)
                            Text(reel.authorName, color = PglobWhite, fontWeight = FontWeight.Bold)
                            Surface(
                                color = if (isFollowing) PglobGreen else PglobGold,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable {
                                    followedAuthors[reel.authorId] = !isFollowing
                                    viewModel.showToast(if (!isFollowing) "Following ${reel.authorName}" else "Unfollowed")
                                }
                            ) {
                                Text(
                                    if (isFollowing) "Following" else "Follow",
                                    color = PglobBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(reel.caption, color = PglobWhite, fontSize = 15.sp)
                        Text("${reel.hashtag}  •  🎵 ${reel.music}", color = PglobGold, fontSize = 12.sp)
                    }

                    // Right Action Rail
                    Column(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                if (!reel.id.startsWith("reel_seed")) viewModel.likeReel(reel)
                                else viewModel.showToast("❤️ Liked ${reel.authorName}'s reel!")
                            }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Like Reel", tint = PglobRed)
                            }
                            Text("${reel.likes}", color = PglobWhite, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { commentReelTarget = reel }) {
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comment", tint = PglobWhite)
                            }
                            Text("${reelComments[reel.id]?.size ?: 0}", color = PglobWhite, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            if ((myProfile?.coins ?: 0L) >= 10L) {
                                viewModel.claimEarnTask("Gift Thank-You", 0L)
                                viewModel.showToast("🌹 Sent Rose Gift to ${reel.authorName}!")
                            } else {
                                viewModel.showToast("Need 10 coins to send a Rose gift")
                            }
                        }) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = "Gift Creator", tint = PglobGold)
                        }
                        IconButton(onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Watch ${reel.authorName} on PGLOB APP Reels: ${reel.caption}")
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Reel"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Reel", tint = PglobWhite)
                        }
                        IconButton(onClick = { viewModel.submitReport("REEL", reel.id, "Reported reel") }) {
                            Icon(Icons.Default.Report, contentDescription = "Report Reel", tint = PglobGreyText)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(90.dp)) }
        }

        FloatingActionButton(
            onClick = { showUpload = true },
            containerColor = PglobGold,
            contentColor = PglobBlack,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("upload_reel_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Upload Reel")
        }
    }

    if (showUpload) {
        AlertDialog(
            onDismissRequest = { showUpload = false },
            containerColor = PglobDarkGrey,
            title = { Text("Upload Reel (+10 Coins)", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("Caption") })
                    OutlinedTextField(value = hashtag, onValueChange = { hashtag = it }, label = { Text("Hashtag") }, singleLine = true)
                    OutlinedTextField(value = music, onValueChange = { music = it }, label = { Text("Music Track") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (caption.isNotBlank()) {
                            viewModel.postReel(caption.trim(), hashtag.trim(), music.trim())
                            caption = ""
                            showUpload = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Publish Reel") }
            },
            dismissButton = {
                TextButton(onClick = { showUpload = false }) { Text("Cancel", color = PglobWhite) }
            }
        )
    }

    commentReelTarget?.let { target ->
        val existingComments = reelComments[target.id].orEmpty()
        AlertDialog(
            onDismissRequest = { commentReelTarget = null },
            containerColor = PglobDarkGrey,
            title = { Text("Comments • ${target.authorName}", color = PglobGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (existingComments.isEmpty()) {
                        Text("Be the first to comment!", color = PglobGreyText)
                    } else {
                        existingComments.forEach { c ->
                            Text("💬 $c", color = PglobWhite, fontSize = 13.sp)
                        }
                    }
                    OutlinedTextField(value = commentInput, onValueChange = { commentInput = it }, label = { Text("Add a comment") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            reelComments[target.id] = existingComments + commentInput.trim()
                            commentInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Post") }
            },
            dismissButton = {
                TextButton(onClick = { commentReelTarget = null }) { Text("Close", color = PglobWhite) }
            }
        )
    }
}

@Composable
fun GamesScreen(
    myProfile: UserPrivateProfile?,
    localSettings: LocalAppSettings,
    viewModel: PglobViewModel
) {
    val scope = rememberCoroutineScope()
    var activeGame by remember { mutableStateOf("Spin") } // Spin, Dice, Quiz, Memory, RPS

    // Spin Wheel state
    val spinAngle = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    val wheelRewards = listOf(10L, 50L, 25L, 100L, 20L, 250L, 50L, 500L)
    var lastSpinResult by remember { mutableStateOf<String?>(null) }

    // Dice state
    var die1 by remember { mutableIntStateOf(6) }
    var die2 by remember { mutableIntStateOf(1) }
    var diceResultText by remember { mutableStateOf("Pick High (8-12), Lucky 7, or Low (2-6) & Roll!") }

    // Quiz state
    var quizIdx by remember { mutableIntStateOf(0) }
    var quizScore by remember { mutableIntStateOf(0) }

    // Memory Match state
    val baseEmojis = remember { listOf("🌍", "💎", "🪙", "👑", "❤️", "🚀") }
    var cards by remember { mutableStateOf((baseEmojis + baseEmojis).shuffled()) }
    var revealedIndices by remember { mutableStateOf(setOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(setOf<Int>()) }

    // RPS state
    var rpsResult by remember { mutableStateOf("Choose Rock, Paper, or Scissors to win +25 coins!") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Play & Win Coins 🎮", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("🪙 ${myProfile?.coins ?: 100L} Coins", color = PglobGreen, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Spin" to "🎡 Spin Wheel", "Dice" to "🎲 Lucky Dice", "Quiz" to "🧠 Trivia", "Memory" to "🃏 Memory", "RPS" to "✊ RPS").forEach { (id, label) ->
                    Surface(
                        color = if (activeGame == id) PglobGold else PglobDarkGrey,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { activeGame = id }
                    ) {
                        Text(label, color = if (activeGame == id) PglobBlack else PglobWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }
        }

        item {
            Surface(
                color = PglobDarkGrey,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, PglobGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (activeGame) {
                        "Spin" -> {
                            Text("Daily Luxury Spin Wheel (10–500 Coins)", color = PglobGold, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sweep = 360f / wheelRewards.size
                                    rotate(spinAngle.value) {
                                        wheelRewards.forEachIndexed { idx, _ ->
                                            drawArc(
                                                color = if (idx % 2 == 0) PglobGold else PglobBlack,
                                                startAngle = idx * sweep,
                                                sweepAngle = sweep,
                                                useCenter = true,
                                                size = Size(size.width, size.height)
                                            )
                                        }
                                    }
                                    drawCircle(PglobWhite, radius = 16f, center = Offset(size.width / 2f, size.height / 2f))
                                }
                                Text("▼", color = PglobRed, fontSize = 24.sp, modifier = Modifier.align(Alignment.TopCenter))
                            }
                            if (lastSpinResult != null) {
                                Text(lastSpinResult!!, color = PglobGreen, fontWeight = FontWeight.Bold)
                            }
                            val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000L)
                            val spinUsed = localSettings.lastSpinEpochDay == today
                            Button(
                                onClick = {
                                    if (!isSpinning) {
                                        isSpinning = true
                                        val pickedIdx = Random.nextInt(wheelRewards.size)
                                        val reward = wheelRewards[pickedIdx]
                                        scope.launch {
                                            val target = spinAngle.value + 1440f + Random.nextInt(360)
                                            spinAngle.animateTo(target, tween(2200, easing = FastOutSlowInEasing))
                                            lastSpinResult = "You won +$reward Coins!"
                                            viewModel.claimEarnTask("Daily Spin Wheel", reward, isSpin = true)
                                            isSpinning = false
                                        }
                                    }
                                },
                                enabled = !isSpinning && !spinUsed,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("spin_wheel_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                            ) {
                                Text(if (spinUsed) "Daily Spin Claimed Today" else if (isSpinning) "Spinning..." else "SPIN NOW 🎡", fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        "Dice" -> {
                            Text("Lucky Dice", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                Surface(color = PglobBlack, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(64.dp).border(2.dp, PglobGold, RoundedCornerShape(14.dp))) {
                                    Box(contentAlignment = Alignment.Center) { Text("$die1", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp) }
                                }
                                Surface(color = PglobBlack, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(64.dp).border(2.dp, PglobGold, RoundedCornerShape(14.dp))) {
                                    Box(contentAlignment = Alignment.Center) { Text("$die2", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp) }
                                }
                            }
                            Text(diceResultText, color = PglobWhite, textAlign = TextAlign.Center)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("LOW" to "Low (2-6)", "SEVEN" to "Lucky 7", "HIGH" to "High (8-12)").forEach { (bet, label) ->
                                    Button(
                                        onClick = {
                                            die1 = Random.nextInt(1, 7)
                                            die2 = Random.nextInt(1, 7)
                                            val sum = die1 + die2
                                            val won = (bet == "LOW" && sum <= 6) || (bet == "SEVEN" && sum == 7) || (bet == "HIGH" && sum >= 8)
                                            if (won) {
                                                val winAmt = if (bet == "SEVEN") 80L else 30L
                                                diceResultText = "Rolled $sum! You WON +$winAmt coins!"
                                                viewModel.claimEarnTask("Lucky Dice Win", winAmt)
                                            } else {
                                                diceResultText = "Rolled $sum! Close — try again!"
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                                    ) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }

                        "Quiz" -> {
                            val q = GlobalCatalogData.quizQuestions[quizIdx % GlobalCatalogData.quizQuestions.size]
                            Text("PGLOB Trivia • Score: $quizScore", color = PglobGold, fontWeight = FontWeight.Bold)
                            Text(q.first, color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                            q.second.forEachIndexed { idx, opt ->
                                OutlinedButton(
                                    onClick = {
                                        if (idx == q.third) {
                                            quizScore += 25
                                            viewModel.claimEarnTask("Trivia Answer", 25L)
                                        } else {
                                            viewModel.showToast("Not quite — next question!")
                                        }
                                        quizIdx++
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(opt, color = PglobWhite)
                                }
                            }
                        }

                        "Memory" -> {
                            Text("Match Pairs to Win +60 Coins", color = PglobGold, fontWeight = FontWeight.Bold)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (r in 0 until 3) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        for (c in 0 until 4) {
                                            val idx = r * 4 + c
                                            val isOpen = idx in revealedIndices || idx in matchedIndices
                                            Surface(
                                                color = if (idx in matchedIndices) PglobGreen.copy(alpha = 0.25f) else PglobBlack,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .size(58.dp)
                                                    .border(1.dp, PglobGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                    .clickable(enabled = !isOpen) {
                                                        val nextRevealed = revealedIndices + idx
                                                        if (nextRevealed.size == 2) {
                                                            val list = nextRevealed.toList()
                                                            if (cards[list[0]] == cards[list[1]]) {
                                                                val nextMatched = matchedIndices + nextRevealed
                                                                matchedIndices = nextMatched
                                                                revealedIndices = emptySet()
                                                                if (nextMatched.size == cards.size) {
                                                                    viewModel.claimEarnTask("Memory Match Clear", 60L)
                                                                }
                                                            } else {
                                                                revealedIndices = setOf(idx)
                                                            }
                                                        } else {
                                                            revealedIndices = nextRevealed
                                                        }
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(if (isOpen) cards[idx] else "❓", fontSize = 22.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            TextButton(onClick = {
                                cards = (baseEmojis + baseEmojis).shuffled()
                                revealedIndices = emptySet()
                                matchedIndices = emptySet()
                            }) { Text("Shuffle Board", color = PglobGold) }
                        }

                        "RPS" -> {
                            Text("Rock Paper Scissors", color = PglobGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(rpsResult, color = PglobWhite, textAlign = TextAlign.Center)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Rock ✊", "Paper ✋", "Scissors ✌️").forEachIndexed { idx, label ->
                                    Button(
                                        onClick = {
                                            val bot = Random.nextInt(3)
                                            val botLabel = listOf("Rock ✊", "Paper ✋", "Scissors ✌️")[bot]
                                            when {
                                                idx == bot -> rpsResult = "Draw! Bot picked $botLabel"
                                                (idx == 0 && bot == 2) || (idx == 1 && bot == 0) || (idx == 2 && bot == 1) -> {
                                                    rpsResult = "YOU WIN vs $botLabel! +25 coins"
                                                    viewModel.claimEarnTask("RPS Win", 25L)
                                                }
                                                else -> rpsResult = "Bot picked $botLabel — Try again!"
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                                    ) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
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

@Composable
fun LearnScreen(
    localSettings: LocalAppSettings,
    viewModel: PglobViewModel
) {
    var activeCourse by remember { mutableStateOf<LearnCourse?>(null) }
    var selectedQuizIdx by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            Text("PGLOB Learn & Earn 🎓", color = PglobGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text("Finish lessons & pass quiz to earn Verified Certificates + Coins", color = PglobGreyText, fontSize = 13.sp)
        }

        items(GlobalCatalogData.courses, key = { it.id }) { course ->
            val completed = localSettings.completedCourses.contains(course.id)
            Surface(
                color = PglobDarkGrey,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (completed) PglobGreen.copy(alpha = 0.6f) else PglobGold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { activeCourse = course; selectedQuizIdx = null }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(PglobGold.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (completed) Icons.Default.CheckCircle else Icons.Default.School,
                            contentDescription = "Course",
                            tint = if (completed) PglobGreen else PglobGold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(course.title, color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${course.instructor} • ${course.duration}", color = PglobGreyText, fontSize = 12.sp)
                        Text(
                            if (completed) "✓ Certificate Earned" else "Reward: +${course.rewardCoins} coins",
                            color = if (completed) PglobGreen else PglobGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                    Text(if (course.priceCoins == 0) "FREE" else "${course.priceCoins}🪙", color = PglobGold, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Spacer(Modifier.height(90.dp)) }
    }

    activeCourse?.let { course ->
        AlertDialog(
            onDismissRequest = { activeCourse = null },
            containerColor = PglobDarkGrey,
            title = { Text(course.title, color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lessons:", color = PglobGold, fontWeight = FontWeight.SemiBold)
                    course.lessons.forEach { l -> Text("• $l", color = PglobWhite, fontSize = 13.sp) }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Text("Certification Quiz:", color = PglobGold, fontWeight = FontWeight.SemiBold)
                    Text(course.quizQuestion, color = PglobWhite, fontSize = 14.sp)
                    course.quizOptions.forEachIndexed { idx, opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedQuizIdx == idx) PglobGold.copy(alpha = 0.25f) else PglobBlack)
                                .clickable { selectedQuizIdx = idx }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opt, color = PglobWhite, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedQuizIdx == course.correctIndex) {
                            viewModel.completeLearnCourse(course.id, course.rewardCoins)
                            activeCourse = null
                        } else {
                            viewModel.showToast("Check the lessons and select the right answer!")
                        }
                    },
                    enabled = selectedQuizIdx != null,
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Claim Certificate (+${course.rewardCoins}🪙)") }
            },
            dismissButton = {
                TextButton(onClick = { activeCourse = null }) { Text("Close", color = PglobWhite) }
            }
        )
    }
}

@Composable
fun JobsScreen(
    jobs: List<JobListing>,
    viewModel: PglobViewModel,
    onOpenChatId: (String) -> Unit
) {
    var filterType by remember { mutableStateOf("All") }
    var showPostJob by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("Remote") }
    var location by remember { mutableStateOf("Worldwide") }
    var desc by remember { mutableStateOf("") }
    val savedJobs = remember { mutableStateMapOf<String, Boolean>() }

    val pool = remember(jobs) {
        if (jobs.isNotEmpty()) jobs else listOf(
            JobListing("job_seed_1", "marcus_us", "Senior Android Compose Engineer", "PGLOB Labs", "$4,500/mo", "Remote", "Worldwide", "Build next-gen global chat, Firebase & Compose UIs."),
            JobListing("job_seed_2", "amina_ng", "Social Media & Reels Growth Lead", "Lagos Luxury Media", "₦450,000/mo", "Full-time", "Lagos / Hybrid", "Grow creator engagement across Africa & Diaspora."),
            JobListing("job_seed_3", "priya_in", "UI/UX & Brand Motion Designer", "Nova Digital", "$35/hr", "Freelance", "Remote", "Design Gold & Black luxury mobile experiences.")
        )
    }
    val filtered = remember(pool, filterType) {
        if (filterType == "All") pool else pool.filter { it.jobType.equals(filterType, true) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Remote", "Full-time", "Part-time", "Freelance").forEach { t ->
                    Surface(
                        color = if (filterType == t) PglobGold else PglobDarkGrey,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.clickable { filterType = t }
                    ) {
                        Text(t, color = if (filterType == t) PglobBlack else PglobWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { job ->
                    Surface(
                        color = PglobDarkGrey,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, PglobGold.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Work, contentDescription = null, tint = PglobGold, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(job.title, color = PglobWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                IconButton(onClick = {
                                    val saved = savedJobs[job.id] == true
                                    savedJobs[job.id] = !saved
                                    viewModel.showToast(if (!saved) "Job bookmarked" else "Removed bookmark")
                                }) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "Save Job", tint = if (savedJobs[job.id] == true) PglobGold else PglobGreyText)
                                }
                            }
                            Text("${job.company} • ${job.location} (${job.jobType})", color = PglobGreyText, fontSize = 13.sp)
                            Text("💰 ${job.salary}", color = PglobGreen, fontWeight = FontWeight.Bold)
                            Text(job.description, color = PglobWhite.copy(alpha = 0.85f), fontSize = 13.sp)
                            Button(
                                onClick = {
                                    viewModel.openOrCreateChatWith(
                                        title = "${job.company} HR",
                                        targetUid = job.posterId,
                                        flag = "💼"
                                    ) { chatId ->
                                        viewModel.sendChatMessage(
                                            chatId,
                                            listOf(viewModel.currentUserId, job.posterId),
                                            "Hello! I am applying for ${job.title} (${job.salary}). Please see my PGLOB profile & CV!"
                                        )
                                        viewModel.showToast("Application & CV sent via Chat!")
                                        onOpenChatId(chatId)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                            ) {
                                Text("Apply with PGLOB Profile & CV", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showPostJob = true },
            containerColor = PglobGold,
            contentColor = PglobBlack,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("post_job_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Job")
        }
    }

    if (showPostJob) {
        AlertDialog(
            onDismissRequest = { showPostJob = false },
            containerColor = PglobDarkGrey,
            title = { Text("Post Global Job", color = PglobGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Role Title") }, singleLine = true)
                    OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company") }, singleLine = true)
                    OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Salary (e.g. $2,000/mo)") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Remote", "Full-time", "Freelance").forEach { jt ->
                            Surface(
                                color = if (jobType == jt) PglobGold else PglobBlack,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { jobType = jt }
                            ) {
                                Text(jt, color = if (jobType == jt) PglobBlack else PglobWhite, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Job Description") }, maxLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && company.isNotBlank()) {
                            viewModel.postJob(title.trim(), company.trim(), salary.ifBlank { "Competitive" }, jobType, location.ifBlank { "Global" }, desc.ifBlank { "Apply via PGLOB APP" })
                            title = ""; company = ""; salary = ""; desc = ""
                            showPostJob = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PglobGold, contentColor = PglobBlack)
                ) { Text("Post Job") }
            },
            dismissButton = {
                TextButton(onClick = { showPostJob = false }) { Text("Cancel", color = PglobWhite) }
            }
        )
    }
}
