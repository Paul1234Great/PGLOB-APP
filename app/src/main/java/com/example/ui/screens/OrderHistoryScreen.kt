package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderDeliveryStatus
import com.example.data.model.OrderHistoryRecord
import com.example.ui.theme.PglobBlack
import com.example.ui.theme.PglobGold
import com.example.ui.theme.PglobGreen
import com.example.ui.theme.PglobRed
import com.example.ui.theme.WaBackground
import com.example.ui.theme.WaCard
import com.example.ui.theme.WaDivider
import com.example.ui.theme.WaGold
import com.example.ui.theme.WaGreen
import com.example.ui.theme.WaInput
import com.example.ui.theme.WaPureBlack
import com.example.ui.theme.WaSolidGoldSurface
import com.example.ui.theme.WaSolidGreenSurface
import com.example.ui.theme.WaSolidRedSurface
import com.example.ui.theme.WaTextPrimary
import com.example.ui.theme.WaTextSecondary
import java.text.NumberFormat
import java.util.Locale

private enum class OrderFilterOption(val tag: String, val label: String) {
    ALL("ALL", "All"),
    PENDING("PENDING", "Pending"),
    ESCROWED("ESCROWED", "Escrowed"),
    DELIVERED("DELIVERED", "Delivered")
}

@Composable
fun OrderHistoryScreen(
    orders: List<OrderHistoryRecord>,
    isRefreshing: Boolean = false,
    syncError: String? = null,
    onBack: () -> Unit,
    onRefresh: () -> Unit = {},
    onConfirmDelivered: (OrderHistoryRecord) -> Unit = {},
    onChatSeller: (OrderHistoryRecord) -> Unit = {},
    onBrowseMarket: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        onRefresh()
    }

    var selectedStatusFilter by remember { mutableStateOf(OrderFilterOption.ALL) }
    var marketOnlyFilter by remember { mutableStateOf(false) }

    val scopedOrders = remember(orders, marketOnlyFilter) {
        if (marketOnlyFilter) orders.filter { it.txType.equals("MARKET", ignoreCase = true) } else orders
    }

    val pendingCount = remember(scopedOrders) {
        scopedOrders.count { it.deliveryStatus == OrderDeliveryStatus.PENDING }
    }
    val escrowedCount = remember(scopedOrders) {
        scopedOrders.count { it.deliveryStatus == OrderDeliveryStatus.ESCROWED }
    }
    val deliveredCount = remember(scopedOrders) {
        scopedOrders.count { it.deliveryStatus == OrderDeliveryStatus.DELIVERED }
    }

    val visibleOrders = remember(scopedOrders, selectedStatusFilter) {
        when (selectedStatusFilter) {
            OrderFilterOption.ALL -> scopedOrders
            OrderFilterOption.PENDING -> scopedOrders.filter { it.deliveryStatus == OrderDeliveryStatus.PENDING }
            OrderFilterOption.ESCROWED -> scopedOrders.filter { it.deliveryStatus == OrderDeliveryStatus.ESCROWED }
            OrderFilterOption.DELIVERED -> scopedOrders.filter { it.deliveryStatus == OrderDeliveryStatus.DELIVERED }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("order_history_screen")
    ) {
        // Header
        Surface(color = WaCard, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("order_history_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WaTextPrimary
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order History",
                        color = WaTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Firestore synced • ${scopedOrders.size} record(s)",
                        color = WaTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("order_history_refresh_btn")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = WaGold,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh orders from Firestore",
                            tint = WaGold
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = WaDivider)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("order_history_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sync status banner if error exists
            if (!syncError.isNullOrBlank()) {
                item {
                    Surface(
                        color = WaSolidRedSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PglobRed, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = syncError,
                                color = WaTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Retry",
                                color = WaGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onRefresh() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 3-Stage Status Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrderKpiCard(
                        label = "Pending",
                        count = pendingCount,
                        accent = WaGold,
                        selected = selectedStatusFilter == OrderFilterOption.PENDING,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_kpi_pending"),
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == OrderFilterOption.PENDING) {
                                OrderFilterOption.ALL
                            } else {
                                OrderFilterOption.PENDING
                            }
                        }
                    )
                    OrderKpiCard(
                        label = "Escrowed",
                        count = escrowedCount,
                        accent = Color(0xFF38BDF8),
                        selected = selectedStatusFilter == OrderFilterOption.ESCROWED,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_kpi_escrowed"),
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == OrderFilterOption.ESCROWED) {
                                OrderFilterOption.ALL
                            } else {
                                OrderFilterOption.ESCROWED
                            }
                        }
                    )
                    OrderKpiCard(
                        label = "Delivered",
                        count = deliveredCount,
                        accent = WaGreen,
                        selected = selectedStatusFilter == OrderFilterOption.DELIVERED,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_kpi_delivered"),
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == OrderFilterOption.DELIVERED) {
                                OrderFilterOption.ALL
                            } else {
                                OrderFilterOption.DELIVERED
                            }
                        }
                    )
                }
            }

            // Scope toggle (All Transactions vs Market Orders)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (!marketOnlyFilter) WaGreen else WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, if (!marketOnlyFilter) WaGreen else WaDivider, RoundedCornerShape(14.dp))
                            .clickable { marketOnlyFilter = false }
                            .testTag("order_scope_all_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "All Transactions",
                                color = if (!marketOnlyFilter) WaPureBlack else WaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Surface(
                        color = if (marketOnlyFilter) WaGold else WaCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, if (marketOnlyFilter) WaGold else WaDivider, RoundedCornerShape(14.dp))
                            .clickable { marketOnlyFilter = true }
                            .testTag("order_scope_market_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Market Orders",
                                color = if (marketOnlyFilter) PglobBlack else WaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Filter Pills Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrderFilterOption.entries.forEach { option ->
                        val active = selectedStatusFilter == option
                        val count = when (option) {
                            OrderFilterOption.ALL -> scopedOrders.size
                            OrderFilterOption.PENDING -> pendingCount
                            OrderFilterOption.ESCROWED -> escrowedCount
                            OrderFilterOption.DELIVERED -> deliveredCount
                        }
                        Surface(
                            color = if (active) WaGold else WaInput,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .clickable { selectedStatusFilter = option }
                                .testTag("order_filter_${option.tag}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "${option.label} ($count)",
                                    color = if (active) PglobBlack else WaTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (visibleOrders.isEmpty()) {
                item {
                    Surface(
                        color = WaCard,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, WaDivider, RoundedCornerShape(18.dp))
                            .testTag("order_history_empty_state")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(WaSolidGoldSurface)
                                    .border(1.dp, WaGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = WaGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = if (scopedOrders.isEmpty()) "No Order History Yet" else "No ${selectedStatusFilter.label} Orders",
                                color = WaTextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Completed Firestore transactions and Market Escrow purchases appear here with Pending, Escrowed, and Delivered tracking.",
                                color = WaTextSecondary,
                                fontSize = 15.sp,
                                lineHeight = 21.sp
                            )
                            Button(
                                onClick = onBrowseMarket,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WaGreen,
                                    contentColor = WaPureBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("order_history_browse_market_btn")
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Browse PGLOB Market", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            } else {
                items(visibleOrders, key = { it.id.ifBlank { "${it.orderReference}_${it.timestampMillis}" } }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onConfirmDelivered = { onConfirmDelivered(order) },
                        onChatSeller = { onChatSeller(order) }
                    )
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun OrderKpiCard(
    label: String,
    count: Int,
    accent: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) accent.copy(alpha = 0.16f) else WaCard,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(1.5.dp, if (selected) accent else WaDivider, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                color = accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Text(
                text = label,
                color = WaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OrderHistoryCard(
    order: OrderHistoryRecord,
    onConfirmDelivered: () -> Unit,
    onChatSeller: () -> Unit
) {
    val statusAccent = when (order.deliveryStatus) {
        OrderDeliveryStatus.PENDING -> WaGold
        OrderDeliveryStatus.ESCROWED -> Color(0xFF38BDF8)
        OrderDeliveryStatus.DELIVERED -> WaGreen
    }
    val statusSurface = when (order.deliveryStatus) {
        OrderDeliveryStatus.PENDING -> WaSolidGoldSurface
        OrderDeliveryStatus.ESCROWED -> Color(0xFF0E2538)
        OrderDeliveryStatus.DELIVERED -> WaSolidGreenSurface
    }
    val amountFormatted = remember(order.amount, order.currency) {
        val num = NumberFormat.getNumberInstance(Locale.US).format(order.amount.toLong())
        if (order.currency.equals("COINS", ignoreCase = true)) "$num Coins" else "${order.currency}$num"
    }
    val txLabel = remember(order.txType) {
        when (order.txType.uppercase(Locale.US)) {
            "MARKET" -> "Market Order"
            "ADD" -> "Wallet Top-up"
            "WITHDRAW" -> "Withdrawal"
            "SEND" -> "Money Sent"
            "RECEIVE" -> "Money Received"
            "GIFT" -> "Gift Order"
            else -> order.txType
        }
    }

    Surface(
        color = WaCard,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WaDivider, RoundedCornerShape(18.dp))
            .testTag("order_card_${order.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Reference + Type + Status Badge
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
                    Text(
                        text = order.orderReference,
                        color = WaGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(color = WaInput, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = txLabel,
                            color = WaTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Surface(
                    color = statusSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .border(1.dp, statusAccent, RoundedCornerShape(10.dp))
                        .testTag("order_status_badge_${order.id}")
                ) {
                    Text(
                        text = order.deliveryStatus.label,
                        color = statusAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Title & Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = order.title,
                        color = WaTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = "Party: ${order.counterpartyName}",
                        color = WaTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${order.paymentMethod} • ${order.dateLabel}",
                        color = WaTextSecondary,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = amountFormatted,
                    color = if (order.deliveryStatus == OrderDeliveryStatus.DELIVERED) PglobGreen else PglobGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                )
            }

            // 3-Stage Order Tracking Bar: Pending -> Escrowed -> Delivered
            OrderStatusTrackingBar(currentStatus = order.deliveryStatus)

            // Action Buttons
            if (order.deliveryStatus != OrderDeliveryStatus.DELIVERED || order.txType.equals("MARKET", ignoreCase = true)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (order.deliveryStatus != OrderDeliveryStatus.DELIVERED) {
                        Button(
                            onClick = onConfirmDelivered,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WaGreen,
                                contentColor = WaPureBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("order_confirm_delivered_${order.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Confirm Delivered", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        }
                    }
                    if (order.txType.equals("MARKET", ignoreCase = true)) {
                        OutlinedButton(
                            onClick = onChatSeller,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("order_chat_seller_${order.id}")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat Seller", tint = WaGold, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Chat Seller", color = WaGold, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusTrackingBar(currentStatus: OrderDeliveryStatus) {
    val activeIndex = when (currentStatus) {
        OrderDeliveryStatus.PENDING -> 0
        OrderDeliveryStatus.ESCROWED -> 1
        OrderDeliveryStatus.DELIVERED -> 2
    }
    Surface(
        color = WaBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WaDivider, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrackingStepNode(
                label = "Pending",
                icon = Icons.Default.Schedule,
                reached = activeIndex >= 0,
                isCurrent = activeIndex == 0,
                accent = WaGold
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .padding(horizontal = 4.dp)
                    .background(
                        if (activeIndex >= 1) Color(0xFF38BDF8) else WaDivider,
                        RoundedCornerShape(2.dp)
                    )
            )
            TrackingStepNode(
                label = "Escrowed",
                icon = Icons.Default.Shield,
                reached = activeIndex >= 1,
                isCurrent = activeIndex == 1,
                accent = Color(0xFF38BDF8)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .padding(horizontal = 4.dp)
                    .background(
                        if (activeIndex >= 2) WaGreen else WaDivider,
                        RoundedCornerShape(2.dp)
                    )
            )
            TrackingStepNode(
                label = "Delivered",
                icon = Icons.Default.LocalShipping,
                reached = activeIndex >= 2,
                isCurrent = activeIndex == 2,
                accent = WaGreen
            )
        }
    }
}

@Composable
private fun TrackingStepNode(
    label: String,
    icon: ImageVector,
    reached: Boolean,
    isCurrent: Boolean,
    accent: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (reached) accent else WaInput)
                .border(1.5.dp, if (isCurrent) WaTextPrimary else if (reached) accent else WaDivider, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (reached && !isCurrent) Icons.Default.Check else icon,
                contentDescription = label,
                tint = if (reached) PglobBlack else WaTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            color = if (reached) WaTextPrimary else WaTextSecondary,
            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
