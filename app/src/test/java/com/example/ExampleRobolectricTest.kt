package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChatMessageEntity
import com.example.data.local.PglobDatabase
import com.example.data.model.AppAdminPermission
import com.example.data.model.AppSubAdmin
import com.example.data.model.GroupSubAdminPermissions
import com.example.data.model.MarketProduct
import com.example.data.model.OrderDeliveryStatus
import com.example.data.model.OrderHistoryRecord
import com.example.data.model.PublicProfile
import com.example.data.model.StatusNote
import com.example.data.model.StatusNoteActor
import com.example.ui.ContributionAdminCodec
import com.example.ui.MarketCheckoutOutcome
import com.example.ui.screens.FullScreenStatusNoteViewer
import com.example.ui.screens.MarketProductCheckoutOverlay
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.PublicUserProfileSheet
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatusNotesHorizontalStrip
import com.example.ui.screens.UserProfileEditorSheet
import com.example.ui.screens.WelcomeAuthScreen
import com.example.ui.screens.WriteStatusNoteOverlay
import com.example.ui.theme.PglobChatTheme
import com.example.util.StatusNoteCodec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PGLOB", appName)
  }

  @Test
  fun `room chat_messages schema stores senderId timestamp and content chronologically`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, PglobDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    try {
      val dao = db.chatMessageDao()
      dao.upsertAll(
        listOf(
          ChatMessageEntity(
            id = "m2",
            chatId = "dm_buyer_amina",
            senderId = "amina_ng",
            senderName = "Amina Gadget Hub",
            memberUidsJoined = "buyer_1,amina_ng",
            messageContent = "Yes, iPhone 15 Pro Max is available in Lagos!",
            timestampMillis = 2000L
          ),
          ChatMessageEntity(
            id = "m1",
            chatId = "dm_buyer_amina",
            senderId = "buyer_1",
            senderName = "Paul Odo",
            memberUidsJoined = "buyer_1,amina_ng",
            messageContent = "Hi! Is iPhone 15 Pro Max available?",
            timestampMillis = 1000L
          )
        )
      )
      val messages = dao.observeMessagesForChat("dm_buyer_amina").first()
      assertEquals(2, messages.size)
      assertEquals("buyer_1", messages[0].senderId)
      assertEquals(1000L, messages[0].timestampMillis)
      assertEquals("Hi! Is iPhone 15 Pro Max available?", messages[0].messageContent)
      assertEquals("amina_ng", messages[1].senderId)
      assertEquals(2000L, messages[1].timestampMillis)
    } finally {
      db.close()
    }
  }

  @Test
  fun `market buy now renders full product detail checkout sticky bar address fields and all 3 products without blank`() {
    val iphone = MarketProduct(
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
    )
    val agbada = MarketProduct(
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
    )
    val mic = MarketProduct(
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

    val selectedProduct = androidx.compose.runtime.mutableStateOf<MarketProduct?>(iphone)
    val walletBal = androidx.compose.runtime.mutableDoubleStateOf(2_000_000.0)
    var coinsBal = 2000L
    var lastCapturedAddress = ""
    var addMoneyTapped = false

    composeTestRule.setContent {
      PglobChatTheme {
        selectedProduct.value?.let { prod ->
          MarketProductCheckoutOverlay(
            product = prod,
            currencySymbol = "₦",
            walletBalance = walletBal.doubleValue,
            coinsBalance = coinsBal,
            defaultLocation = "Lagos, Nigeria",
            defaultCountry = "Nigeria",
            defaultPhone = "+2348012345678",
            onDismiss = { selectedProduct.value = null },
            onChatSeller = {},
            onAddMoney = { addMoneyTapped = true },
            onConfirmPurchase = { qty, delivery, address, feeCash, method ->
              lastCapturedAddress = address
              when (method) {
                "WALLET" -> {
                  val total = (prod.priceCash * qty) + feeCash
                  if (walletBal.doubleValue >= total) {
                    walletBal.doubleValue -= total
                    MarketCheckoutOutcome.Success(
                      orderNumber = "PGLOB-MKT-001234",
                      productTitle = prod.title,
                      sellerName = prod.sellerName,
                      quantity = qty,
                      deliveryOption = delivery,
                      paymentMethod = "PGLOB Wallet (₦)",
                      paidSummary = "₦${total.toLong()} deducted from Wallet",
                      escrowHoldSummary = "Funds locked safely in PGLOB Escrow."
                    )
                  } else {
                    MarketCheckoutOutcome.Failure("Insufficient Wallet", "PGLOB Wallet")
                  }
                }
                "COINS" -> {
                  val totalCoins = (prod.priceCoins * qty) + if (feeCash > 0) 5 else 0
                  if (coinsBal >= totalCoins) {
                    coinsBal -= totalCoins
                    MarketCheckoutOutcome.Success(
                      orderNumber = "PGLOB-MKT-005678",
                      productTitle = prod.title,
                      sellerName = prod.sellerName,
                      quantity = qty,
                      deliveryOption = delivery,
                      paymentMethod = "PGLOB Coins",
                      paidSummary = "$totalCoins Coins deducted",
                      escrowHoldSummary = "Coins locked safely in PGLOB Escrow."
                    )
                  } else {
                    MarketCheckoutOutcome.Failure("Insufficient Coins", "PGLOB Coins")
                  }
                }
                else -> MarketCheckoutOutcome.Success(
                  orderNumber = "PGLOB-MKT-009012",
                  productTitle = prod.title,
                  sellerName = prod.sellerName,
                  quantity = qty,
                  deliveryOption = delivery,
                  paymentMethod = "Cash on Delivery",
                  paidSummary = "Pay on delivery",
                  escrowHoldSummary = "Order protected by PGLOB Escrow."
                )
              }
            }
          )
        }
      }
    }

    // 1) Sticky Bottom Bar is immediately visible WITHOUT scrolling!
    composeTestRule.onNodeWithTag("market_checkout_overlay").assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_sticky_bottom_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_confirm_pay_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_chat_seller_btn").assertIsDisplayed()
    composeTestRule.onNodeWithText("Product Detail & Checkout").assertIsDisplayed()
    composeTestRule.onAllNodesWithText("iPhone 15 Pro Max 256GB (Clean)")[0].performScrollTo().assertIsDisplayed()

    // 2) Scrollable Full Delivery Address (Country, City, Street, Phone) + Full Payment Method Cards + Order Summary
    composeTestRule.onNodeWithTag("checkout_city_input").performScrollTo().performTextReplacement("Abuja")
    composeTestRule.onNodeWithTag("checkout_street_input").performScrollTo().performTextReplacement("24 Wuse II Crescent")
    composeTestRule.onNodeWithTag("checkout_phone_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Pay rider upon verified inspection").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_order_summary_card").performScrollTo().assertIsDisplayed()

    // 3) Confirm & Pay from sticky bar succeeds with PGLOB Escrow order receipt
    composeTestRule.onNodeWithTag("checkout_confirm_pay_btn").performClick()
    composeTestRule.onNodeWithTag("checkout_order_success_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Order No: PGLOB-MKT-001234").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Money dey PGLOB Escrow 🔒").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_view_orders_btn").performScrollTo().assertIsDisplayed()
    assertTrue(lastCapturedAddress.contains("24 Wuse II Crescent"))
    assertTrue(lastCapturedAddress.contains("Abuja"))

    // 4) Switch to Royal Gold Handwoven Agbada Set, check Pickup mode copy, and pay with Coins
    composeTestRule.runOnUiThread { selectedProduct.value = agbada }
    composeTestRule.waitForIdle()
    composeTestRule.onAllNodesWithText("Royal Gold Handwoven Agbada Set")[0].performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_delivery_Pickup_/_Meet_Seller").performScrollTo().performClick()
    composeTestRule.onNodeWithText("You go meet seller for agreed place - Chat seller to agree place").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_pay_method_COINS").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("checkout_confirm_pay_btn").performClick()
    composeTestRule.onNodeWithTag("checkout_order_success_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Order No: PGLOB-MKT-005678").performScrollTo().assertIsDisplayed()

    // 5) Switch to Creator Pro Mic + Ring Light Kit with low wallet balance -> verifies "Balance No Reach - Add Money"
    composeTestRule.runOnUiThread {
      walletBal.doubleValue = 0.0
      selectedProduct.value = mic
    }
    composeTestRule.waitForIdle()
    composeTestRule.onAllNodesWithText("Creator Pro Mic + Ring Light Kit")[0].performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Balance No Reach - Add Money").assertIsDisplayed()
    composeTestRule.onNodeWithTag("checkout_add_money_btn").assertIsDisplayed().performClick()
    assertTrue(addMoneyTapped)
  }

  @Test
  fun `contribution and sub-admin codecs round trip cleanly`() {
    val contributions = ContributionAdminCodec.defaultContributions("u_paul", "Paul Odo", "paulodogun").mapIndexed { index, group ->
      if (index == 0) {
        group.copy(
          groupIconEmoji = "👑",
          rulesText = "Custom rule: pay by Friday 6PM.",
          agreedMemberUids = listOf("u_paul", "m_blessing"),
          escrowFrozen = true,
          paymentAccountMode = "LEGACY_ADMIN_PERSONAL"
        )
      } else group
    }
    val encodedContribs = ContributionAdminCodec.encodeContributions(contributions)
    val decodedContribs = ContributionAdminCodec.parseContributions(encodedContribs)
    assertNotNull(decodedContribs)
    assertEquals(contributions.size, decodedContribs!!.size)
    assertEquals(contributions.first().name, decodedContribs.first().name)
    assertEquals(contributions.first().members.size, decodedContribs.first().members.size)
    assertEquals(contributions.first().totalPotPerRound, decodedContribs.first().totalPotPerRound, 0.01)
    assertEquals("👑", decodedContribs.first().groupIconEmoji)
    assertEquals("Custom rule: pay by Friday 6PM.", decodedContribs.first().rulesText)
    assertEquals(listOf("u_paul", "m_blessing"), decodedContribs.first().agreedMemberUids)
    assertTrue(decodedContribs.first().escrowFrozen)
    assertEquals("LEGACY_ADMIN_PERSONAL", decodedContribs.first().paymentAccountMode)
    assertEquals("APPROVED", decodedContribs.first().members.first().paymentApprovalStatus)
    assertEquals("WAITING_APPROVAL", decodedContribs.first().members[8].paymentApprovalStatus)
    assertEquals("COLLECTED", decodedContribs[1].members.first().collectionStatus)
    assertTrue(decodedContribs.first().auditLog.isNotEmpty())

    val appSubs = listOf(
      AppSubAdmin(
        uid = "sub_1",
        name = "Amina Yusuf",
        email = "amina@pglob.app",
        username = "amina_lagos",
        roleLabel = "Finance Sub-Admin",
        permissions = setOf(
          AppAdminPermission.VIEW_DASHBOARD,
          AppAdminPermission.APPROVE_PAYOUTS,
          AppAdminPermission.VIEW_ESCROW_BALANCE
        )
      )
    )
    val decodedAppSubs = ContributionAdminCodec.parseAppSubAdmins(
      ContributionAdminCodec.encodeAppSubAdmins(appSubs)
    )
    assertNotNull(decodedAppSubs)
    assertEquals(1, decodedAppSubs!!.size)
    assertEquals(appSubs.first().permissions, decodedAppSubs.first().permissions)
    assertEquals("paulodogun197@gmail.com", decodedAppSubs.first().addedByEmail)

    val groupSubs = listOf(
      GroupSubAdminPermissions(
        groupId = "ajo_benin_ladies",
        userUid = "m_amina",
        userName = "Amina Yusuf",
        canRemindMembers = true,
        canAddRemoveMember = true,
        canChangeTurnOrder = false,
        cannotChangeAmount = true,
        cannotEndGroup = true,
        canApproveNewMember = true
      )
    )
    val decodedGroupSubs = ContributionAdminCodec.parseGroupSubAdmins(
      ContributionAdminCodec.encodeGroupSubAdmins(groupSubs)
    )
    assertEquals(1, decodedGroupSubs.size)
    assertTrue(decodedGroupSubs.first().canRemindMembers)
    assertFalse(decodedGroupSubs.first().canChangeTurnOrder)
    assertTrue(decodedGroupSubs.first().cannotChangeAmount)
    assertTrue(decodedGroupSubs.first().cannotEndGroup)
    assertTrue(decodedGroupSubs.first().canApproveNewMember)
  }

  @Test
  fun `order history screen renders pending escrowed delivered tracking and confirms delivery`() {
    val initialOrders = listOf(
      OrderHistoryRecord(
        id = "ord_1",
        orderReference = "PGLOB-MKT-110022",
        title = "1x Creator Pro Mic + Ring Light Kit",
        counterpartyName = "Vance Studio Gear",
        txType = "MARKET",
        amount = 45000.0,
        fee = 0.0,
        currency = "₦",
        paymentMethod = "Cash on Delivery",
        deliveryStatus = OrderDeliveryStatus.PENDING,
        rawStatus = "PENDING",
        note = "Cash order",
        timestampMillis = 1000L,
        dateLabel = "Nov 16, 2026 • 1:00 PM"
      ),
      OrderHistoryRecord(
        id = "ord_2",
        orderReference = "PGLOB-MKT-334455",
        title = "1x iPhone 15 Pro Max 256GB (Clean)",
        counterpartyName = "Amina Gadget Hub",
        txType = "MARKET",
        amount = 1250000.0,
        fee = 0.0,
        currency = "₦",
        paymentMethod = "PGLOB Wallet Escrow",
        deliveryStatus = OrderDeliveryStatus.ESCROWED,
        rawStatus = "PAID",
        note = "Escrow hold",
        timestampMillis = 2000L,
        dateLabel = "Nov 16, 2026 • 2:00 PM"
      ),
      OrderHistoryRecord(
        id = "ord_3",
        orderReference = "PGLOB-MKT-667788",
        title = "1x Royal Gold Handwoven Agbada Set",
        counterpartyName = "Kwame Luxury Kente",
        txType = "MARKET",
        amount = 85000.0,
        fee = 0.0,
        currency = "₦",
        paymentMethod = "PGLOB Coins Escrow",
        deliveryStatus = OrderDeliveryStatus.DELIVERED,
        rawStatus = "APPROVED",
        note = "Delivered",
        timestampMillis = 3000L,
        dateLabel = "Nov 15, 2026 • 5:00 PM"
      )
    )

    val ordersState = androidx.compose.runtime.mutableStateOf(initialOrders)
    var refreshCalled = false

    composeTestRule.setContent {
      PglobChatTheme {
        OrderHistoryScreen(
          orders = ordersState.value,
          onBack = {},
          onRefresh = { refreshCalled = true },
          onConfirmDelivered = { clicked ->
            ordersState.value = ordersState.value.map {
              if (it.id == clicked.id) it.copy(deliveryStatus = OrderDeliveryStatus.DELIVERED) else it
            }
          },
          onChatSeller = {},
          onBrowseMarket = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    assertTrue(refreshCalled)
    composeTestRule.onNodeWithTag("order_history_screen").assertIsDisplayed()
    composeTestRule.onNodeWithText("Order History").assertIsDisplayed()
    composeTestRule.onNodeWithTag("order_history_list").performScrollToNode(hasTestTag("order_card_ord_1"))
    composeTestRule.onNodeWithTag("order_card_ord_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("order_history_list").performScrollToNode(hasTestTag("order_card_ord_2"))
    composeTestRule.onNodeWithTag("order_card_ord_2").assertIsDisplayed()

    // Scroll back to filter row and filter to Escrowed only -> ord_2 visible, ord_1 hidden
    composeTestRule.onNodeWithTag("order_history_list").performScrollToNode(hasTestTag("order_filter_ESCROWED"))
    composeTestRule.onNodeWithTag("order_filter_ESCROWED").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("order_history_list").performScrollToNode(hasTestTag("order_card_ord_2"))
    composeTestRule.onNodeWithTag("order_card_ord_2").assertIsDisplayed()
    composeTestRule.onAllNodesWithText("PGLOB-MKT-110022").fetchSemanticsNodes().let { nodes ->
      assertEquals(0, nodes.size)
    }

    // Confirm delivery on Escrowed order ord_2
    composeTestRule.onNodeWithTag("order_confirm_delivered_ord_2").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Switch to Delivered filter -> ord_2 now appears under Delivered
    composeTestRule.onNodeWithTag("order_history_list").performScrollToNode(hasTestTag("order_filter_DELIVERED"))
    composeTestRule.onNodeWithTag("order_filter_DELIVERED").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("order_history_list").performScrollToNode(hasTestTag("order_card_ord_2"))
    composeTestRule.onNodeWithTag("order_card_ord_2").assertIsDisplayed()
  }

  @Test
  fun `status note strip shows add expired and active states`() {
    val activeMineState = androidx.compose.runtime.mutableStateOf<StatusNote?>(null)
    val expiredMineState = androidx.compose.runtime.mutableStateOf<StatusNote?>(null)
    val friendNote = StatusNoteCodec.defaultFriendStatusNotes().first()
    var addTapped = 0

    composeTestRule.setContent {
      PglobChatTheme {
        StatusNotesHorizontalStrip(
          notes = listOf(friendNote),
          currentUserId = "u_paul",
          myName = "Paul Odo",
          myPhotoUri = "",
          myActiveNote = activeMineState.value,
          myExpiredNote = expiredMineState.value,
          onAddOrReplaceNote = { addTapped++ },
          onOpenNote = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("status_notes_strip").assertIsDisplayed()
    composeTestRule.onNodeWithText("Your Note: + Add Note").assertIsDisplayed()
    composeTestRule.onNodeWithTag("status_note_item_${friendNote.id}").assertIsDisplayed()

    composeTestRule.onNodeWithTag("status_note_my_slot").performClick()
    assertEquals(1, addTapped)

    // Expired state -> shows "Your Note don expire" + "Write New Note"
    composeTestRule.runOnUiThread {
      expiredMineState.value = StatusNoteCodec.createNewNote(
        userId = "u_paul",
        authorName = "Paul Odo",
        authorAvatarUri = "",
        text = "Old note",
        bgColor = "#00A884",
        music = "",
        mood = "",
        privacy = "EVERYONE",
        customAllowedUids = emptyList(),
        durationHours = 24,
        noteId = "expired_1"
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Your Note don expire").assertIsDisplayed()
    composeTestRule.onNodeWithTag("status_note_write_new_btn").assertIsDisplayed().performClick()
    assertEquals(2, addTapped)

    // Active state -> shows my active bubble text
    composeTestRule.runOnUiThread {
      expiredMineState.value = null
      activeMineState.value = StatusNoteCodec.createNewNote(
        userId = "u_paul",
        authorName = "Paul Odo",
        authorAvatarUri = "",
        text = "Chilling in Benin",
        bgColor = "#FFD700",
        music = "",
        mood = "",
        privacy = "EVERYONE",
        customAllowedUids = emptyList(),
        durationHours = 24,
        noteId = "active_1"
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Chilling in Benin").assertIsDisplayed()
  }

  @Test
  fun `write status note enforces 100 char limit mood privacy duration and posts`() {
    var postedText = ""
    var postedColor = ""
    var postedMood = ""
    var postedPrivacy = ""
    var postedHours = 0

    composeTestRule.setContent {
      PglobChatTheme {
        WriteStatusNoteOverlay(
          existingNote = null,
          isEditingSameId = false,
          isProUser = true,
          publicProfiles = emptyList(),
          currentUserId = "u_paul",
          onDismiss = {},
          onSubmitNewOrReplace = { text, bgColor, _, mood, privacy, _, durationHours ->
            postedText = text
            postedColor = bgColor
            postedMood = mood
            postedPrivacy = privacy
            postedHours = durationHours
          },
          onSubmitEdit = { _, _, _, _, _, _, _, _ -> }
        )
      }
    }

    composeTestRule.onNodeWithText("Write Your Status Note").assertIsDisplayed()
    composeTestRule.onNodeWithText("0/100").assertIsDisplayed()

    val over100 = "X".repeat(120)
    composeTestRule.onNodeWithTag("write_note_textfield").performTextReplacement(over100)
    composeTestRule.onNodeWithText("100/100").assertIsDisplayed()

    composeTestRule.onNodeWithTag("write_note_textfield").performTextReplacement("Lagos hustle today")
    composeTestRule.onNodeWithText("18/100").assertIsDisplayed()

    composeTestRule.onNodeWithTag("write_note_color_gold").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("write_note_mood_section").performScrollTo()
    composeTestRule.onNodeWithTag("write_note_mood_hot").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("write_note_privacy_section").performScrollTo()
    composeTestRule.onNodeWithTag("write_note_privacy_ajo_group").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("write_note_duration_section").performScrollTo()
    composeTestRule.onNodeWithTag("write_note_duration_48").performScrollTo().performClick()

    composeTestRule.onNodeWithTag("write_note_post_btn").performClick()
    assertEquals("Lagos hustle today", postedText)
    assertEquals("#FFD700", postedColor)
    assertEquals("🔥 Hot", postedMood)
    assertEquals("AJO_GROUP", postedPrivacy)
    assertEquals(48, postedHours)
  }

  @Test
  fun `full screen note viewer handles real likes viewers sheet reshare 4 options and delete dialog`() {
    val myNote = StatusNoteCodec.createNewNote(
      userId = "u_paul",
      authorName = "Paul Odo",
      authorAvatarUri = "",
      text = "I dey Lagos today, DM for Ajo 🔥",
      bgColor = "#00A884",
      music = "Afrobeats Mix 🎵",
      mood = "🔥 Hot",
      privacy = "EVERYONE",
      customAllowedUids = emptyList(),
      durationHours = 24,
      noteId = "my_note_1"
    ).copy(
      viewers = listOf(StatusNoteActor("u_amina", "Amina Yusuf", "", System.currentTimeMillis() - 120_000L))
    )
    val noteState = androidx.compose.runtime.mutableStateOf(myNote)
    var deletedNoteId = ""

    composeTestRule.setContent {
      PglobChatTheme {
        FullScreenStatusNoteViewer(
          notes = listOf(noteState.value),
          initialNoteId = "my_note_1",
          currentUserId = "u_paul",
          canAdminModerate = true,
          chats = emptyList(),
          contributions = emptyList(),
          onDismiss = {},
          onRecordView = {},
          onToggleLike = { current ->
            val liked = current.likes.any { it.uid == "u_paul" }
            val next = if (liked) {
              current.likes.filterNot { it.uid == "u_paul" }
            } else {
              current.likes + StatusNoteActor("u_paul", "Paul Odo", "", System.currentTimeMillis())
            }
            noteState.value = current.copy(likes = next)
          },
          onReshareToMyNote = {},
          onReshareToMyStatus = {},
          onShareToChat = { _, _, _, _ -> },
          onReplyToNote = { _, _ -> },
          onReplaceNote = {},
          onEditNote = {},
          onDeleteNote = { deletedNoteId = it },
          onReportNote = { _, _ -> },
          onBlockAuthor = {},
          onAdminDeleteNote = {},
          onShowMessage = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("full_screen_note_viewer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_viewer_main_text").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Seen by 1 people").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("❤️ 0 Likes").performScrollTo().assertIsDisplayed()

    // Tap Like -> count becomes 1 (no fake likes!)
    composeTestRule.onNodeWithTag("note_viewer_like_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("❤️ 1 Likes").performScrollTo().assertIsDisplayed()

    // Tap Likers pill -> opens Likers sheet showing Paul Odo
    composeTestRule.onNodeWithTag("note_viewer_likes_count_btn").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("note_actor_list_sheet").assertIsDisplayed()
    composeTestRule.onAllNodesWithText("Paul Odo").let { assertTrue(it.fetchSemanticsNodes().isNotEmpty()) }

    // Dismiss sheet via close button
    composeTestRule.onNodeWithTag("note_actor_sheet_close_btn").performClick()
    composeTestRule.waitForIdle()

    // Open Reshare 4 options sheet
    composeTestRule.onNodeWithTag("note_viewer_reshare_btn").performClick()
    composeTestRule.onNodeWithTag("note_reshare_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_reshare_opt_my_note").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_reshare_opt_my_status").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_reshare_opt_chat").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_reshare_opt_external").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_reshare_copy_link_btn").performScrollTo().assertIsDisplayed()

    composeTestRule.onNodeWithTag("note_reshare_close_btn").performClick()
    composeTestRule.waitForIdle()

    // Tap Delete -> "Delete this Note?" -> Yes -> deletes note
    composeTestRule.onNodeWithTag("note_own_delete_btn").performScrollTo().performClick()
    composeTestRule.onNodeWithText("Delete this Note?").assertIsDisplayed()
    composeTestRule.onNodeWithTag("note_delete_confirm_yes").performClick()
    assertEquals("my_note_1", deletedNoteId)
  }

  @Test
  fun `status note codec json round trip preserves actors edited and reshare credit`() {
    val now = 1_700_000_000_000L
    val base = StatusNoteCodec.createNewNote(
      userId = "u_paul",
      authorName = "Paul Odo",
      authorAvatarUri = "",
      text = "I dey Lagos today, DM for Ajo 🔥",
      bgColor = "#00A884",
      music = "Afrobeats Mix 🎵",
      mood = "🔥 Hot",
      privacy = "EVERYONE",
      customAllowedUids = emptyList(),
      durationHours = 24,
      nowMillis = now,
      noteId = "note_paul_1"
    ).copy(likes = listOf(StatusNoteActor("u_amina", "Amina", "", now + 500L)))

    val edited = StatusNoteCodec.editNoteKeepingSameId(
      existing = base,
      text = "Updated Abuja meetup 🔥",
      bgColor = "#EC4899",
      music = "Amapiano Vibes 🎶",
      mood = "😊 Happy",
      privacy = "EVERYONE",
      customAllowedUids = emptyList(),
      durationHours = 24,
      nowMillis = now + 60_000L
    )
    val reshared = StatusNoteCodec.buildResharedNote(
      original = edited,
      resharerUid = "u_zara",
      resharerName = "Zara Bella",
      resharerAvatarUri = "",
      nowMillis = now + 120_000L,
      newNoteId = "note_zara_reshare"
    )

    val json = StatusNoteCodec.encodeNotesJson(listOf(edited, reshared))
    val parsed = StatusNoteCodec.parseNotesJson(json)
    assertEquals(2, parsed.size)
    assertEquals("note_paul_1", parsed[0].id)
    assertTrue(parsed[0].edited)
    assertEquals(1, parsed[0].likes.size)
    assertEquals("Paul Odo", parsed[1].resharedFromName)
  }

  @Test
  fun `user profile editor uploads photo writes clamped bio toggles interests and saves`() {
    var galleryTriggered = false
    var cameraTriggered = false
    var savedName = ""
    var savedBio = ""
    var savedInterests = emptyList<String>()

    composeTestRule.setContent {
      PglobChatTheme {
        UserProfileEditorSheet(
          initialName = "Paul Odo",
          initialAbout = "Hey there! I am using PGLOB.",
          initialBio = "",
          initialInterests = listOf("Afrobeats"),
          initialPhotoUri = "",
          initialCountryLabel = "Nigeria (+234)",
          initialPhone = "+2348012345678",
          onDismiss = {},
          onPickPhotoGallery = { galleryTriggered = true },
          onPickPhotoCamera = { cameraTriggered = true },
          onRemovePhoto = {},
          onPickCountry = {},
          onSave = { name, _, bio, interests, _ ->
            savedName = name
            savedBio = bio
            savedInterests = interests
          }
        )
      }
    }

    composeTestRule.onNodeWithTag("user_profile_editor_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_editor_gallery_btn").performScrollTo().performClick()
    assertTrue(galleryTriggered)
    composeTestRule.onNodeWithTag("profile_editor_camera_btn").performScrollTo().performClick()
    assertTrue(cameraTriggered)

    // Bio over 200 chars is clamped cleanly to 200/200
    composeTestRule.onNodeWithTag("profile_editor_bio_input").performScrollTo().performTextReplacement("Z".repeat(220))
    composeTestRule.onNodeWithText("200/200").assertIsDisplayed()

    // Realistic short bio + suggested interest + custom interest
    composeTestRule.onNodeWithTag("profile_editor_bio_input").performScrollTo().performTextReplacement("Building Ajo escrow & global fintech in Lagos.")
    composeTestRule.onNodeWithTag("suggested_interest_tech___ai").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("profile_editor_interest_input").performScrollTo().performTextReplacement("Comedy")
    composeTestRule.onNodeWithTag("profile_editor_add_interest_btn").performScrollTo().performClick()

    composeTestRule.onNodeWithTag("profile_editor_save_btn").performScrollTo().performClick()
    assertEquals("Paul Odo", savedName)
    assertEquals("Building Ajo escrow & global fintech in Lagos.", savedBio)
    assertEquals(listOf("Afrobeats", "Tech & AI", "Comedy"), savedInterests)
  }

  @Test
  fun `public user profile sheet displays other user photo bio interests and triggers message`() {
    val peer = PublicProfile(
      userId = "amina_ng",
      displayName = "Amina Yusuf",
      username = "amina_lagos",
      usernameVerified = true,
      country = "Nigeria",
      countryFlag = "🇳🇬",
      city = "Lagos",
      language = "English",
      vipTier = "GOLD",
      bio = "Lagos entrepreneur hosting verified weekly Ajo circles.",
      interests = listOf("Ajo Savings", "Business", "Afrobeats")
    )
    var messageClicked = false

    composeTestRule.setContent {
      PglobChatTheme {
        PublicUserProfileSheet(
          profile = peer,
          isOwnProfile = false,
          isBlocked = false,
          onDismiss = {},
          onMessageUser = { messageClicked = true },
          onReportUser = {},
          onToggleBlock = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("public_user_profile_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithText("Member Profile").assertIsDisplayed()
    composeTestRule.onNodeWithText("Amina Yusuf").assertIsDisplayed()
    composeTestRule.onNodeWithText("@amina_lagos").assertIsDisplayed()
    composeTestRule.onNodeWithTag("public_profile_bio_text").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Lagos entrepreneur hosting verified weekly Ajo circles.").assertIsDisplayed()
    composeTestRule.onNodeWithTag("public_profile_interests_flow").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Ajo Savings").assertIsDisplayed()
    composeTestRule.onNodeWithText("Business").assertIsDisplayed()
    composeTestRule.onNodeWithText("Afrobeats").assertIsDisplayed()

    composeTestRule.onNodeWithTag("public_profile_message_btn").performScrollTo().performClick()
    assertTrue(messageClicked)
  }

  @Test
  fun `splash displays green pglob logo and from PGLOB for 2s then routes to welcome agree and continue`() {
    val showSplash = androidx.compose.runtime.mutableStateOf(true)

    composeTestRule.setContent {
      PglobChatTheme {
        if (showSplash.value) {
          SplashScreen(onFinished = { showSplash.value = false })
        } else {
          WelcomeAuthScreen(onAuthSuccess = {})
        }
      }
    }

    // 1. Splash screen visible with Green Circle PGLOB logo + "from PGLOB" footer, zero other brand names
    composeTestRule.onNodeWithTag("pglob_splash_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pglob_splash_logo").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pglob_splash_from_footer").assertIsDisplayed()
    composeTestRule.onNodeWithText("from PGLOB").assertIsDisplayed()
    composeTestRule.onAllNodesWithText("Loading...").fetchSemanticsNodes().let { assertEquals(0, it.size) }

    // Still showing before 2 seconds elapse
    assertTrue(showSplash.value)

    // Advances to Welcome screen once 2-second splash completes
    composeTestRule.waitUntil(timeoutMillis = 3_500L) { !showSplash.value }
    composeTestRule.waitForIdle()
    assertFalse(showSplash.value)

    // 2. Welcome screen shows PGLOB logo, title, tagline, privacy text, and AGREE AND CONTINUE -> Sign Up
    composeTestRule.onNodeWithTag("welcome_pglob_logo").assertIsDisplayed()
    composeTestRule.onNodeWithText("Welcome to PGLOB").assertIsDisplayed()
    composeTestRule.onNodeWithText("Chat • Pay • Earn • Ajo • Market - All in One App").assertIsDisplayed()
    composeTestRule.onNodeWithText("By continuing, you agree to our Privacy Policy and Terms").assertIsDisplayed()
    composeTestRule.onNodeWithText("AGREE AND CONTINUE").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Create PGLOB Account").assertIsDisplayed()
  }
}
