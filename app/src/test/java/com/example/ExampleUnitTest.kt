package com.example

import com.example.data.model.ContributionAuditEntry
import com.example.data.model.ContributionGroup
import com.example.data.model.ContributionMember
import com.example.data.model.GroupSubAdminPermissions
import com.example.data.model.OrderDeliveryStatus
import com.example.data.model.PayTransaction
import com.example.data.model.StatusNoteActor
import com.example.ui.ContributionAdminCodec
import com.example.ui.OrderHistoryMapper
import com.example.util.ContributionCodec
import com.example.util.CountryPayCatalog
import com.example.util.PasswordStrength
import com.example.util.PglobStrings
import com.example.util.PolicyCatalog
import com.example.util.ProfileCodec
import com.example.util.SecurityAndNumberCodec
import com.example.util.StatusNoteCodec
import com.example.util.StatusQuoteCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun localizedStrings_coverNigerianAndGlobalLanguages() {
        assertEquals("PGLOB APP", PglobStrings.forLanguage("English").appTitle)
        assertEquals("Toris", PglobStrings.forLanguage("Pidgin").chats)
        assertEquals("Àwọn Ìfọ̀rọ̀wérọ̀", PglobStrings.forLanguage("Yoruba").chats)
        assertEquals("Fassara", PglobStrings.forLanguage("Hausa").translate)
        assertEquals("Zipu Ego", PglobStrings.forLanguage("Igbo").sendMoney)
        assertEquals("Discussions", PglobStrings.forLanguage("French").chats)
        assertEquals("Traducir", PglobStrings.forLanguage("Spanish").translate)
        assertEquals("Conversas", PglobStrings.forLanguage("Portuguese").chats)
        assertEquals("الدردشات", PglobStrings.forLanguage("Arabic").chats)
    }

    @Test
    fun countryPayCatalog_adaptsMethodsBanksAndLookup() {
        assertTrue(CountryPayCatalog.methodsForCountry("Nigeria").contains("OPay"))
        assertTrue(CountryPayCatalog.methodsForCountry("Kenya").contains("M-Pesa"))
        assertTrue(CountryPayCatalog.banksForCountry("United Kingdom").contains("Monzo"))
        assertEquals(
            "PAUL ODOGUN • OPay",
            CountryPayCatalog.lookupAccountName("2087491984", "OPay", "Member")
        )
        assertEquals(100_000.0, CountryPayCatalog.largeTransferThreshold("₦"), 0.01)
    }

    @Test
    fun policyCatalog_containsAllCorePolicies() {
        val ids = PolicyCatalog.allPolicies.map { it.id }
        assertTrue(ids.containsAll(listOf("privacy", "terms", "payment", "community", "cookies")))
    }

    @Test
    fun statusQuoteCodec_encodesDecodesAndFormatsShareUrl() {
        val encodedReply = StatusQuoteCodec.encodeReply(
            statusId = "st_zara123",
            ownerName = "Zara Bella",
            ownerHandle = "ZaraBella",
            mediaType = "IMAGE",
            mediaUri = "https://example.com/photo.jpg",
            caption = "Luxury Gold vibes across London"
        )
        assertTrue(encodedReply.length <= 500)
        val parsedReply = StatusQuoteCodec.decodeOrNull(encodedReply)
        assertNotNull(parsedReply)
        assertFalse(parsedReply!!.isForward)
        assertEquals("st_zara123", parsedReply.statusId)
        assertEquals("Zara Bella", parsedReply.ownerName)
        assertEquals("ZaraBella", parsedReply.ownerHandle)
        assertEquals("IMAGE", parsedReply.mediaType)
        assertEquals("Luxury Gold vibes across London", parsedReply.caption)

        val encodedForward = StatusQuoteCodec.encodeForward(
            statusId = "st_marcus",
            ownerName = "Marcus Vance",
            ownerHandle = "MarcusV",
            mediaType = "VIDEO",
            mediaUri = "",
            caption = "Global Pay Demo"
        )
        val parsedForward = StatusQuoteCodec.decodeOrNull(encodedForward)
        assertNotNull(parsedForward)
        assertTrue(parsedForward!!.isForward)
        assertEquals("st_marcus", parsedForward.statusId)

        assertNull(StatusQuoteCodec.decodeOrNull("Regular message text"))
        assertEquals(
            "https://pglob.app/status/zara123",
            StatusQuoteCodec.statusShareUrl("st_zara123", "ZaraBella", "Zara Bella")
        )
        assertEquals(
            "https://pglob.app/status/amina_y",
            StatusQuoteCodec.statusShareUrl("st_amina", "amina_y", "Amina Yusuf")
        )
    }

    @Test
    fun contributionGroup_calculatesTotalPotAndTrustScoreClamping() {
        val members = List(10) { idx ->
            ContributionMember(
                uid = "u_$idx",
                name = "Member $idx",
                handle = "m_$idx",
                turnOrder = idx + 1
            )
        }
        val group = ContributionGroup(
            id = "ajo_1",
            name = "Benin Ladies Ajo",
            purpose = "Savings",
            amountPerRound = 10_000.0,
            members = members
        )
        assertEquals(100_000.0, group.totalPotPerRound, 0.01)

        assertEquals(95, ContributionAdminCodec.nextTrustScore(90, onTime = true))
        assertEquals(100, ContributionAdminCodec.nextTrustScore(98, onTime = true))
        assertEquals(80, ContributionAdminCodec.nextTrustScore(90, onTime = false))
        assertEquals(70, ContributionAdminCodec.nextTrustScore(90, onTime = false, missed = true))
        assertEquals(0, ContributionAdminCodec.nextTrustScore(10, onTime = false, missed = true))
        assertEquals("paulodogun197@gmail.com", ContributionAdminCodec.SUPER_ADMIN_EMAIL)
    }

    @Test
    fun contributionCodec_encodesAndDecodesChatCardPayload() {
        val encoded = ContributionCodec.encode(
            groupId = "ajo_benin_ladies",
            title = "Benin Ladies Ajo",
            subtitle = "Round 1/10 • ₦10,000 • Paid",
            badge = "ESCROW"
        )
        val decoded = ContributionCodec.decodeOrNull(encoded)
        assertNotNull(decoded)
        assertEquals("ajo_benin_ladies", decoded!!.groupId)
        assertEquals("Benin Ladies Ajo", decoded.title)
        assertEquals("Round 1/10 • ₦10,000 • Paid", decoded.subtitle)
        assertEquals("ESCROW", decoded.badge)
        assertNull(ContributionCodec.decodeOrNull("Normal chat message"))
    }

    @Test
    fun securityAndNumberCodec_validatesPasswordPhoneDistinctAccountAndMasking() {
        assertEquals(PasswordStrength.TOO_SHORT, SecurityAndNumberCodec.validatePassword("abc12"))
        assertEquals(PasswordStrength.WEAK, SecurityAndNumberCodec.validatePassword("abcdefgh"))
        assertEquals(PasswordStrength.WEAK, SecurityAndNumberCodec.validatePassword("12345678"))
        assertEquals(PasswordStrength.GOOD, SecurityAndNumberCodec.validatePassword("pglob1234"))
        assertEquals(PasswordStrength.STRONG, SecurityAndNumberCodec.validatePassword("Pglob@2026"))

        assertFalse(SecurityAndNumberCodec.isPasswordAcceptable("1234567"))
        assertFalse(SecurityAndNumberCodec.isPasswordAcceptable("onlyletters"))
        assertTrue(SecurityAndNumberCodec.isPasswordAcceptable("pglob123"))

        assertTrue(SecurityAndNumberCodec.isValidPhoneForCountry("+234", "08012345678"))
        assertFalse(SecurityAndNumberCodec.isValidPhoneForCountry("+234", "12345"))
        assertTrue(SecurityAndNumberCodec.isValidPhoneForCountry("+1", "2125550199"))
        assertTrue(SecurityAndNumberCodec.isValidPhoneForCountry("+44", "07700900123"))
        assertEquals("+234 8012345678", SecurityAndNumberCodec.normalizePhoneWithDial("+234", "08012345678"))

        val phone = "+234 8012345678"
        val generatedAccount = SecurityAndNumberCodec.generateDistinctAccountNumber(phone, seed = "paul_seed")
        assertEquals(10, generatedAccount.length)
        assertTrue(generatedAccount.all { it.isDigit() })
        assertNotEquals("8012345678", generatedAccount)

        assertTrue(SecurityAndNumberCodec.looksLikeChattingPhoneInsteadOfAccount("+234 8012345678", phone))
        assertTrue(SecurityAndNumberCodec.looksLikeChattingPhoneInsteadOfAccount("08012345678", phone))
        assertFalse(SecurityAndNumberCodec.looksLikeChattingPhoneInsteadOfAccount("9087654321", phone))

        assertEquals("9041***812", SecurityAndNumberCodec.maskAccountNumber("9041234812"))
    }

    @Test
    fun contribution48hAutoApproveAndSubAdminSafetyLocksWorkCorrectly() {
        val perms = GroupSubAdminPermissions(
            groupId = "ajo_1",
            userUid = "u_sub",
            userName = "Amina"
        )
        assertFalse(perms.canApproveNewMember)
        assertTrue(perms.cannotChangeAmount)
        assertTrue(perms.cannotEndGroup)

        val defaultGroup = ContributionGroup(
            id = "g1",
            name = "Test Circle",
            purpose = "Save",
            amountPerRound = 5_000.0
        )
        assertFalse(defaultGroup.allowSubAdminApproveRegistration)

        val now = 1_750_000_000_000L
        assertEquals("48h passed • Auto-Approve ready", ContributionAdminCodec.formatRemaining48h(now - ContributionAdminCodec.AUTO_APPROVE_WINDOW_MS - 1_000L, now))
        assertTrue(ContributionAdminCodec.formatRemaining48h(now - 3_600_000L, now).endsWith("left"))

        // 1) Test Wallet proof > 48h -> AUTO_APPROVED + escrow credited; External proof > 48h -> UNDER_REVIEW
        val expiredTime = now - ContributionAdminCodec.AUTO_APPROVE_WINDOW_MS - 60_000L
        val payGroup = ContributionGroup(
            id = "g_pay",
            name = "Escrow Circle",
            purpose = "Test",
            amountPerRound = 10_000.0,
            totalEscrowBalance = 0.0,
            members = listOf(
                ContributionMember(
                    uid = "u_wallet",
                    name = "Blessing",
                    handle = "blessing",
                    turnOrder = 1,
                    paymentApprovalStatus = "WAITING_APPROVAL",
                    paymentSubmittedAtMillis = expiredTime,
                    paymentProofRef = "PGLOB Wallet Balance"
                ),
                ContributionMember(
                    uid = "u_ext",
                    name = "Tolu",
                    handle = "tolu",
                    turnOrder = 2,
                    paymentApprovalStatus = "WAITING_APPROVAL",
                    paymentSubmittedAtMillis = expiredTime,
                    paymentProofRef = "Bank Transfer EXT-991"
                )
            )
        )
        val (afterPayGroups, payEvents) = ContributionAdminCodec.evaluateAutoApprove48h(listOf(payGroup), now)
        val updatedPayGroup = afterPayGroups.first()
        assertEquals("AUTO_APPROVED", updatedPayGroup.members[0].paymentApprovalStatus)
        assertTrue(updatedPayGroup.members[0].paidCurrentRound)
        assertEquals(10_000.0, updatedPayGroup.totalEscrowBalance, 0.01)
        assertEquals("UNDER_REVIEW", updatedPayGroup.members[1].paymentApprovalStatus)
        assertFalse(updatedPayGroup.members[1].paidCurrentRound)
        assertEquals(2, payEvents.size)

        // 2) Test Collection REQUESTED > 48h when all members paid -> AUTO_COLLECTED & escrow released
        val collectGroup = ContributionGroup(
            id = "g_collect",
            name = "Payout Circle",
            purpose = "Test",
            amountPerRound = 20_000.0,
            totalEscrowBalance = 40_000.0,
            currentRound = 1,
            totalRounds = 2,
            members = listOf(
                ContributionMember(
                    uid = "u_turn",
                    name = "Grace",
                    handle = "grace",
                    turnOrder = 1,
                    paidCurrentRound = true,
                    paymentApprovalStatus = "APPROVED",
                    collectionStatus = "REQUESTED",
                    collectionRequestedAtMillis = expiredTime
                ),
                ContributionMember(
                    uid = "u_other",
                    name = "Zara",
                    handle = "zara",
                    turnOrder = 2,
                    paidCurrentRound = true,
                    paymentApprovalStatus = "APPROVED"
                )
            ),
            auditLog = emptyList<ContributionAuditEntry>()
        )
        val (afterCollectGroups, collectEvents) = ContributionAdminCodec.evaluateAutoApprove48h(listOf(collectGroup), now)
        val updatedCollect = afterCollectGroups.first()
        assertEquals("AUTO_COLLECTED", updatedCollect.members[0].collectionStatus)
        assertTrue(updatedCollect.members[0].hasCollected)
        assertEquals(0.0, updatedCollect.totalEscrowBalance, 0.01)
        assertEquals(2, updatedCollect.currentRound)
        assertTrue(collectEvents.any { it.contains("Auto-Released") })
    }

    @Test
    fun orderHistoryMapper_tracksPendingEscrowedAndDeliveredStagesCorrectly() {
        val pendingTx = PayTransaction(
            id = "tx_pending_1",
            userId = "u_paul",
            txType = "MARKET",
            amount = 45000.0,
            currency = "₦",
            method = "Cash on Delivery (Escrow Verified)",
            destination = "Vance Studio Gear",
            status = "PENDING",
            note = "PGLOB-MKT-102938 • 1x Creator Pro Mic (Home Delivery)"
        )
        val escrowedTx = PayTransaction(
            id = "tx_escrowed_2",
            userId = "u_paul",
            txType = "MARKET",
            amount = 1250000.0,
            currency = "₦",
            method = "PGLOB Wallet Escrow",
            destination = "Amina Gadget Hub",
            status = "PAID",
            note = "PGLOB-MKT-884422 • 1x iPhone 15 Pro Max 256GB • Escrow Safe"
        )
        val approvedTx = PayTransaction(
            id = "tx_delivered_3",
            userId = "u_paul",
            txType = "MARKET",
            amount = 85000.0,
            currency = "₦",
            method = "PGLOB Wallet Escrow",
            destination = "Kwame Luxury Kente",
            status = "APPROVED",
            note = "PGLOB-MKT-771122 • 1x Royal Gold Agbada"
        )

        val r1 = OrderHistoryMapper.mapTransactionToOrderRecord(pendingTx)
        val r2 = OrderHistoryMapper.mapTransactionToOrderRecord(escrowedTx)
        val r3 = OrderHistoryMapper.mapTransactionToOrderRecord(approvedTx)
        val r2Confirmed = OrderHistoryMapper.mapTransactionToOrderRecord(
            tx = escrowedTx,
            deliveredOrderIds = setOf("tx_escrowed_2")
        )

        assertEquals(OrderDeliveryStatus.PENDING, r1.deliveryStatus)
        assertEquals("PGLOB-MKT-102938", r1.orderReference)
        assertEquals(OrderDeliveryStatus.ESCROWED, r2.deliveryStatus)
        assertEquals("PGLOB-MKT-884422", r2.orderReference)
        assertEquals(OrderDeliveryStatus.DELIVERED, r3.deliveryStatus)
        assertEquals(OrderDeliveryStatus.DELIVERED, r2Confirmed.deliveryStatus)
    }

    @Test
    fun statusNoteCodec_enforcesCharLimitNoFakeLikesPrivacyExpiryEditAndReshare() {
        val longRaw = "A".repeat(125)
        val clamped = StatusNoteCodec.clampNoteText(longRaw)
        assertEquals(100, clamped.length)
        assertEquals("100/100", StatusNoteCodec.formatCharCounter(longRaw))
        assertEquals("0/100", StatusNoteCodec.formatCharCounter(""))

        // Zero fake likes/reshares/viewers on seeded friend notes
        val now = 1_750_000_000_000L
        val defaults = StatusNoteCodec.defaultFriendStatusNotes(now)
        assertTrue(defaults.isNotEmpty())
        defaults.forEach { n ->
            assertEquals(0, n.likes.size)
            assertEquals(0, n.reshares.size)
            assertEquals(0, n.viewers.size)
        }

        // Create 24h note vs 48h PRO note
        val note24 = StatusNoteCodec.createNewNote(
            userId = "u_paul",
            authorName = "Paul Odo",
            authorAvatarUri = "",
            text = "I dey Lagos today, DM for Ajo 🔥",
            bgColor = "#00A884",
            music = "Afrobeats Mix 🎵",
            mood = "🔥 Hot",
            privacy = "AJO_GROUP",
            customAllowedUids = emptyList(),
            durationHours = 24,
            nowMillis = now,
            noteId = "note_paul_1"
        )
        assertEquals(24, note24.durationHours)
        assertEquals(0, note24.likes.size)
        assertFalse(note24.edited)
        assertFalse(note24.isExpiredAt(now + 23 * 3_600_000L))
        assertTrue(note24.isExpiredAt(now + 24 * 3_600_000L + 1_000L))

        val note48 = StatusNoteCodec.createNewNote(
            userId = "u_paul",
            authorName = "Paul Odo",
            authorAvatarUri = "",
            text = "PRO 48h note",
            bgColor = "#FFD700",
            music = "",
            mood = "",
            privacy = "CUSTOM",
            customAllowedUids = listOf("u_amina"),
            durationHours = 48,
            nowMillis = now,
            noteId = "note_paul_48"
        )
        assertEquals(48, note48.durationHours)
        assertFalse(note48.isPublic)
        assertFalse(note48.isExpiredAt(now + 47 * 3_600_000L))
        assertTrue(note48.isExpiredAt(now + 48 * 3_600_000L + 1_000L))

        // Privacy checks
        assertTrue(
            StatusNoteCodec.isNoteVisibleToUser(
                note = note24,
                viewerUid = "u_ajo_mate",
                contactUids = emptySet(),
                ajoMemberUids = setOf("u_paul", "u_ajo_mate"),
                blockedUids = emptySet(),
                nowMillis = now + 1_000L
            )
        )
        assertFalse(
            StatusNoteCodec.isNoteVisibleToUser(
                note = note24,
                viewerUid = "u_stranger",
                contactUids = setOf("u_paul"),
                ajoMemberUids = emptySet(),
                blockedUids = emptySet(),
                nowMillis = now + 1_000L
            )
        )
        assertTrue(
            StatusNoteCodec.isNoteVisibleToUser(
                note = note48,
                viewerUid = "u_amina",
                contactUids = emptySet(),
                ajoMemberUids = emptySet(),
                blockedUids = emptySet(),
                nowMillis = now + 1_000L
            )
        )
        assertFalse(
            StatusNoteCodec.isNoteVisibleToUser(
                note = note48,
                viewerUid = "u_amina",
                contactUids = emptySet(),
                ajoMemberUids = emptySet(),
                blockedUids = setOf("u_paul"),
                nowMillis = now + 1_000L
            )
        )

        // Edit keeps SAME Note ID + existing likes, marks edited = true
        val likedNote = note24.copy(
            likes = listOf(StatusNoteActor("u_amina", "Amina", "", now + 500L))
        )
        val edited = StatusNoteCodec.editNoteKeepingSameId(
            existing = likedNote,
            text = "Updated Abuja meetup 🔥",
            bgColor = "#EC4899",
            music = "Amapiano Vibes 🎶",
            mood = "😊 Happy",
            privacy = "EVERYONE",
            customAllowedUids = emptyList(),
            durationHours = 24,
            nowMillis = now + 60_000L
        )
        assertEquals("note_paul_1", edited.id)
        assertEquals(1, edited.likes.size)
        assertTrue(edited.edited)
        assertEquals("Updated Abuja meetup 🔥", edited.text)
        assertEquals("#EC4899", edited.bgColor)

        // Reshare keeps original credit & starts with 0 likes/reshares
        val reshared = StatusNoteCodec.buildResharedNote(
            original = edited,
            resharerUid = "u_zara",
            resharerName = "Zara Bella",
            resharerAvatarUri = "",
            nowMillis = now + 120_000L,
            newNoteId = "note_zara_reshare"
        )
        assertEquals("note_zara_reshare", reshared.id)
        assertEquals("Paul Odo", reshared.resharedFromName)
        assertEquals("note_paul_1", reshared.resharedFromNoteId)
        assertEquals(0, reshared.likes.size)
        assertEquals(0, reshared.reshares.size)
    }

    @Test
    fun profileCodec_clampsBioNormalizesInterestsAndRoundTrips() {
        val rawBio = "B".repeat(250)
        assertEquals(ProfileCodec.MAX_BIO_LENGTH, ProfileCodec.sanitizeBio(rawBio).length)
        assertEquals("200/200", ProfileCodec.formatBioCounter(rawBio))
        assertEquals("0/200", ProfileCodec.formatBioCounter(""))

        assertEquals("Tech & AI", ProfileCodec.normalizeInterestTag("  Tech  & | AI\n "))
        var list = ProfileCodec.addInterest(emptyList(), "Afrobeats")
        list = ProfileCodec.addInterest(list, "afrobeats") // case-insensitive duplicate ignored
        assertEquals(listOf("Afrobeats"), list)

        for (i in 1..15) {
            list = ProfileCodec.addInterest(list, "Topic $i")
        }
        assertEquals(ProfileCodec.MAX_INTERESTS_COUNT, list.size)

        list = ProfileCodec.removeInterest(list, "afrobeats")
        assertEquals(ProfileCodec.MAX_INTERESTS_COUNT - 1, list.size)
        assertFalse(list.any { it.equals("Afrobeats", ignoreCase = true) })

        val encoded = ProfileCodec.encodeInterestsString(list + listOf("Music | Live", "music   live"))
        val decoded = ProfileCodec.decodeInterestsString(encoded)
        assertEquals(ProfileCodec.MAX_INTERESTS_COUNT, decoded.size)
        assertTrue(decoded.contains("Music Live"))
    }
}


