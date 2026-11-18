package com.example.ui

import com.example.data.model.AdminActivityLog
import com.example.data.model.AppAdminConfig
import com.example.data.model.AppAdminPermission
import com.example.data.model.AppSubAdmin
import com.example.data.model.BroadcastAnnouncement
import com.example.data.model.ContributionAuditEntry
import com.example.data.model.ContributionGroup
import com.example.data.model.ContributionMember
import com.example.data.model.ContributionRoundHistory
import com.example.data.model.ContributionStatus
import com.example.data.model.ContributionType
import com.example.data.model.GroupSubAdminPermissions
import com.example.data.model.PayoutOrderType
import com.example.data.model.StatusInteractionTracker
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ContributionAdminCodec {
    const val SUPER_ADMIN_EMAIL = "paulodogun197@gmail.com"
    const val AUTO_APPROVE_WINDOW_MS = 48L * 60 * 60 * 1000L

    fun defaultContributions(currentUserId: String, currentUserName: String, currentUserHandle: String): List<ContributionGroup> {
        val meName = currentUserName.ifBlank { "Paul Odo" }
        val meHandle = currentUserHandle.ifBlank { "paulodogun" }
        val now = System.currentTimeMillis()
        val beninMembers = listOf(
            ContributionMember("m_blessing", "Blessing Gold", "blessing_gold", "", 1, "ADMIN", 98, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Oct 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Blessing Gold", approvedAtLabel = "Today 9:10 AM"),
            ContributionMember(currentUserId, meName, meHandle, "", 2, "MEMBER", 94, paidCurrentRound = false, hasCollected = false, paidRoundsCount = 0, joinedDateLabel = "Oct 2025", paymentApprovalStatus = "UNPAID"),
            ContributionMember("m_amina", "Amina Yusuf", "amina_lagos", "", 3, "SUB_ADMIN", 96, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Oct 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Blessing Gold", approvedAtLabel = "Today 10:05 AM"),
            ContributionMember("m_grace", "Grace Edo", "grace_benin", "", 4, "MEMBER", 90, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Oct 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Blessing Gold", approvedAtLabel = "Today 10:30 AM"),
            ContributionMember("m_joy", "Joy Osagie", "joy_o", "", 5, "MEMBER", 86, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Nov 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Blessing Gold", approvedAtLabel = "Today 10:45 AM"),
            ContributionMember("m_faith", "Faith Igbinovia", "faith_ig", "", 6, "MEMBER", 82, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Nov 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Blessing Gold", approvedAtLabel = "Today 11:00 AM"),
            ContributionMember("m_mercy", "Mercy Ojo", "mercy_ojo", "", 7, "MEMBER", 76, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Nov 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Blessing Gold", approvedAtLabel = "Today 11:10 AM"),
            ContributionMember("m_zara", "Zara Bella", "zarabella", "", 8, "MEMBER", 92, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Nov 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Blessing Gold", approvedAtLabel = "Today 11:20 AM"),
            ContributionMember("m_chioma", "Chioma Nwosu", "chioma_n", "", 9, "MEMBER", 68, paidCurrentRound = false, hasCollected = false, paidRoundsCount = 0, lateCount = 1, joinedDateLabel = "Nov 2025", paymentApprovalStatus = "WAITING_APPROVAL", paymentSubmittedAtMillis = now - 3_600_000L, paymentProofRef = "Bank Transfer • OPY-882910"),
            ContributionMember("m_tolu", "Tolu Ade", "tolu_ade", "", 10, "MEMBER", 38, paidCurrentRound = false, hasCollected = false, paidRoundsCount = 0, lateCount = 2, missedCount = 1, joinedDateLabel = "Nov 2025", paymentApprovalStatus = "UNPAID")
        )
        val lagosMembers = listOf(
            ContributionMember("m_marcus", "Marcus Vance", "marcus_nyc", "", 1, "ADMIN", 95, paidCurrentRound = true, hasCollected = true, paidRoundsCount = 2, joinedDateLabel = "Sep 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = "Marcus Vance", approvedAtLabel = "01 Nov", collectionStatus = "COLLECTED", collectedAtLabel = "01 Nov", collectedApprovedBy = "PGLOB Escrow"),
            ContributionMember(currentUserId, meName, meHandle, "", 2, "ADMIN", 94, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 2, joinedDateLabel = "Sep 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = meName, approvedAtLabel = "Today 8:40 AM"),
            ContributionMember("m_kwame", "Kwame Mensah", "kwame_gh", "", 3, "MEMBER", 89, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 2, joinedDateLabel = "Sep 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = meName, approvedAtLabel = "Today 9:05 AM"),
            ContributionMember("m_priya", "Priya Sharma", "priya_tech", "", 4, "MEMBER", 93, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 2, joinedDateLabel = "Sep 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = meName, approvedAtLabel = "Today 9:22 AM"),
            ContributionMember("m_chidi", "Chidi Okafor", "chidi_dev", "", 5, "MEMBER", 87, paidCurrentRound = false, hasCollected = false, paidRoundsCount = 1, joinedDateLabel = "Oct 2025", paymentApprovalStatus = "UNPAID"),
            ContributionMember("m_liam", "Liam Carter", "liam_uk", "", 6, "MEMBER", 91, paidCurrentRound = true, hasCollected = false, paidRoundsCount = 2, joinedDateLabel = "Oct 2025", paymentApprovalStatus = "APPROVED", approvedByAdminName = meName, approvedAtLabel = "Today 9:50 AM")
        )
        return listOf(
            ContributionGroup(
                id = "ajo_benin_ladies",
                name = "Benin Ladies Ajo",
                purpose = "Weekly business capital & savings rotation",
                type = ContributionType.WEEKLY,
                amountPerRound = 10_000.0,
                currency = "₦",
                members = beninMembers,
                maxMembers = 10,
                currentRound = 1,
                totalRounds = 10,
                totalEscrowBalance = 70_000.0,
                startDateLabel = "Fri 14 Nov",
                endDateLabel = "Fri 16 Jan",
                nextDueDateLabel = "Fri 21 Nov",
                daysRemaining = 2,
                payoutOrderType = PayoutOrderType.ROTATION,
                status = ContributionStatus.ACTIVE,
                requireLateFee = true,
                lateFeeAmount = 500.0,
                mustPayBeforeTurn = true,
                adminCanRemoveUnpaid = true,
                screenshotBlockEnabled = true,
                linkedChatId = "grp_ajo_benin_$currentUserId",
                creatorUid = "m_blessing",
                pendingJoinRequests = listOf("Adaeze Obi (@adaeze_o)"),
                groupIconEmoji = "💎",
                rulesText = "1. Weekly ₦10,000 due every Friday 6:00 PM.\n2. All funds stay locked inside PGLOB Escrow — Group Admin cannot withdraw.\n3. Admin approves proof within 48h or PGLOB auto-approves Wallet payments.\n4. Missed payment without notice deducts 10 Trust points.",
                agreedMemberUids = listOf("m_blessing", currentUserId, "m_amina"),
                allowSubAdminApproveRegistration = false,
                paymentAccountMode = "PGLOB_ESCROW",
                roundHistory = listOf(
                    ContributionRoundHistory("h1", 1, "m_blessing", "Blessing Gold", 10_000.0, 0.0, "₦", "PGLOB Wallet Escrow", "Today 9:10 AM", true, receiptNumber = "PG-AJO-1001", escrowStatus = "IN_ESCROW", approvedByName = "Blessing Gold", maskedAccountNumber = "9041***812"),
                    ContributionRoundHistory("h2", 1, "m_amina", "Amina Yusuf", 10_000.0, 0.0, "₦", "Bank Transfer", "Today 10:05 AM", true, receiptNumber = "PG-AJO-1002", escrowStatus = "IN_ESCROW", approvedByName = "Blessing Gold", maskedAccountNumber = "9082***441"),
                    ContributionRoundHistory("h3", 1, "m_zara", "Zara Bella", 10_000.0, 0.0, "₦", "PGLOB Wallet Escrow", "Today 11:20 AM", true, receiptNumber = "PG-AJO-1003", escrowStatus = "IN_ESCROW", approvedByName = "Blessing Gold", maskedAccountNumber = "9017***305")
                ),
                auditLog = listOf(
                    ContributionAuditEntry("au1", "Blessing Gold", 10_000.0, "₦", "Today 9:10 AM", "PGLOB Escrow Locked", "Blessing Gold (Admin)", receiptNumber = "PG-AJO-1001"),
                    ContributionAuditEntry("au2", "Amina Yusuf", 10_000.0, "₦", "Today 10:05 AM", "PGLOB Escrow Locked", "Blessing Gold (Admin)", receiptNumber = "PG-AJO-1002"),
                    ContributionAuditEntry("au3", "Zara Bella", 10_000.0, "₦", "Today 11:20 AM", "PGLOB Escrow Locked", "Blessing Gold (Admin)", receiptNumber = "PG-AJO-1003")
                )
            ),
            ContributionGroup(
                id = "ajo_lagos_tech",
                name = "Lagos Tech Founders Ajo",
                purpose = "Equipment & server expansion circle",
                type = ContributionType.MONTHLY,
                amountPerRound = 50_000.0,
                currency = "₦",
                members = lagosMembers,
                maxMembers = 6,
                currentRound = 2,
                totalRounds = 6,
                totalEscrowBalance = 250_000.0,
                startDateLabel = "01 Oct",
                endDateLabel = "01 Mar",
                nextDueDateLabel = "30 Nov",
                daysRemaining = 5,
                payoutOrderType = PayoutOrderType.ROTATION,
                status = ContributionStatus.ACTIVE,
                requireLateFee = true,
                lateFeeAmount = 2_000.0,
                mustPayBeforeTurn = true,
                adminCanRemoveUnpaid = true,
                screenshotBlockEnabled = true,
                linkedChatId = "grp_ajo_lagos_$currentUserId",
                creatorUid = currentUserId,
                groupIconEmoji = "🚀",
                rulesText = "1. Monthly ₦50,000 contribution on or before the 30th.\n2. Escrow balance released by Admin directly to current turn member's PGLOB Wallet.\n3. No personal bank collection allowed.",
                agreedMemberUids = lagosMembers.map { it.uid },
                allowSubAdminApproveRegistration = false,
                paymentAccountMode = "PGLOB_ESCROW",
                roundHistory = listOf(
                    ContributionRoundHistory("h4", 1, "m_marcus", "Marcus Vance", 300_000.0, 0.0, "₦", "PGLOB Escrow Release", "01 Nov", true, isPayoutEvent = true, payoutRecipientName = "Marcus Vance", receiptNumber = "PG-AJO-2001", escrowStatus = "RELEASED_TO_MEMBER", approvedByName = meName, maskedAccountNumber = "9124***609")
                ),
                auditLog = listOf(
                    ContributionAuditEntry("au4", "Round 1 Contributors (6/6)", 300_000.0, "₦", "01 Nov 10:00 AM", "PGLOB Escrow -> Released", meName, releasedToName = "Marcus Vance", releasedAtLabel = "01 Nov 2:15 PM", receiptNumber = "PG-AJO-2001")
                )
            )
        )
    }

    fun defaultAppSubAdmins(): List<AppSubAdmin> = listOf(
        AppSubAdmin(
            uid = "sub_amina",
            name = "Amina Yusuf",
            email = "amina.yusuf@pglob.app",
            username = "amina_lagos",
            roleLabel = "Finance Sub-Admin",
            permissions = setOf(
                AppAdminPermission.VIEW_DASHBOARD,
                AppAdminPermission.MANAGE_CONTRIBUTIONS,
                AppAdminPermission.APPROVE_PAYOUTS,
                AppAdminPermission.VIEW_ESCROW_BALANCE,
                AppAdminPermission.VIEW_TRANSACTIONS
            ),
            addedAtLabel = "10 Nov 2025"
        ),
        AppSubAdmin(
            uid = "sub_marcus",
            name = "Marcus Vance",
            email = "marcus.vance@pglob.app",
            username = "marcus_nyc",
            roleLabel = "Moderation Sub-Admin",
            permissions = setOf(
                AppAdminPermission.VIEW_DASHBOARD,
                AppAdminPermission.MANAGE_USERS,
                AppAdminPermission.HANDLE_REPORTS,
                AppAdminPermission.SEND_BROADCAST
            ),
            addedAtLabel = "12 Nov 2025"
        )
    )

    fun defaultAdminLogs(): List<AdminActivityLog> = listOf(
        AdminActivityLog(
            id = "log_1",
            actorEmail = SUPER_ADMIN_EMAIL,
            actorName = "Paul Odo",
            actorRole = "Super Admin",
            actionTitle = "Initialized PGLOB Escrow Engine",
            details = "Activated Ajo Auto-Collect & Payout rules (0.5% fee)",
            timestampLabel = "Today 8:00 AM"
        ),
        AdminActivityLog(
            id = "log_2",
            actorEmail = "amina.yusuf@pglob.app",
            actorName = "Amina Yusuf",
            actorRole = "Sub-Admin",
            actionTitle = "Verified Escrow Batch #402",
            details = "Approved ₦300,000 Round 1 payout for Lagos Tech Founders Ajo",
            timestampLabel = "Today 10:14 AM"
        )
    )

    fun defaultBroadcasts(): List<BroadcastAnnouncement> = listOf(
        BroadcastAnnouncement(
            id = "bc_1",
            title = "PGLOB Group Contribution (Ajo) Live!",
            message = "Create automated Daily, Weekly, or Monthly circles secured by PGLOB Escrow with Trust Scores & instant PDF receipts.",
            targetScope = "ALL_USERS",
            sentByName = "Paul Odo (Super Admin)",
            sentAtLabel = "Today 9:00 AM"
        )
    )

    fun encodeContributions(list: List<ContributionGroup>): String {
        val arr = JSONArray()
        list.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("purpose", g.purpose)
                put("type", g.type.name)
                put("amountPerRound", g.amountPerRound)
                put("currency", g.currency)
                put("maxMembers", g.maxMembers)
                put("currentRound", g.currentRound)
                put("totalRounds", g.totalRounds)
                put("totalEscrowBalance", g.totalEscrowBalance)
                put("startDateLabel", g.startDateLabel)
                put("endDateLabel", g.endDateLabel)
                put("nextDueDateLabel", g.nextDueDateLabel)
                put("daysRemaining", g.daysRemaining)
                put("payoutOrderType", g.payoutOrderType.name)
                put("status", g.status.name)
                put("requireLateFee", g.requireLateFee)
                put("lateFeeAmount", g.lateFeeAmount)
                put("mustPayBeforeTurn", g.mustPayBeforeTurn)
                put("adminCanRemoveUnpaid", g.adminCanRemoveUnpaid)
                put("screenshotBlockEnabled", g.screenshotBlockEnabled)
                put("linkedChatId", g.linkedChatId)
                put("creatorUid", g.creatorUid)
                put("createdAtMillis", g.createdAtMillis)
                put("groupPhotoUri", g.groupPhotoUri)
                put("groupIconEmoji", g.groupIconEmoji)
                put("rulesText", g.rulesText)
                put("agreedMemberUids", JSONArray(g.agreedMemberUids))
                put("allowSubAdminApproveRegistration", g.allowSubAdminApproveRegistration)
                put("escrowFrozen", g.escrowFrozen)
                put("paymentAccountMode", g.paymentAccountMode)
                put("pendingJoinRequests", JSONArray(g.pendingJoinRequests))
                val membersArr = JSONArray()
                g.members.forEach { m ->
                    membersArr.put(JSONObject().apply {
                        put("uid", m.uid)
                        put("name", m.name)
                        put("handle", m.handle)
                        put("photoUri", m.photoUri)
                        put("turnOrder", m.turnOrder)
                        put("role", m.role)
                        put("trustScore", m.trustScore)
                        put("paidCurrentRound", m.paidCurrentRound)
                        put("hasCollected", m.hasCollected)
                        put("paidRoundsCount", m.paidRoundsCount)
                        put("lateCount", m.lateCount)
                        put("missedCount", m.missedCount)
                        put("joinedDateLabel", m.joinedDateLabel)
                        put("paymentApprovalStatus", m.paymentApprovalStatus)
                        put("paymentSubmittedAtMillis", m.paymentSubmittedAtMillis)
                        put("paymentProofRef", m.paymentProofRef)
                        put("paymentProofUri", m.paymentProofUri)
                        put("approvedByAdminName", m.approvedByAdminName)
                        put("approvedAtLabel", m.approvedAtLabel)
                        put("rejectionReason", m.rejectionReason)
                        put("collectionStatus", m.collectionStatus)
                        put("collectionRequestedAtMillis", m.collectionRequestedAtMillis)
                        put("collectedAtLabel", m.collectedAtLabel)
                        put("collectedApprovedBy", m.collectedApprovedBy)
                    })
                }
                put("members", membersArr)
                val histArr = JSONArray()
                g.roundHistory.forEach { h ->
                    histArr.put(JSONObject().apply {
                        put("id", h.id)
                        put("roundNumber", h.roundNumber)
                        put("payerUid", h.payerUid)
                        put("payerName", h.payerName)
                        put("amount", h.amount)
                        put("lateFee", h.lateFee)
                        put("currency", h.currency)
                        put("paymentMethod", h.paymentMethod)
                        put("paidAtLabel", h.paidAtLabel)
                        put("wasOnTime", h.wasOnTime)
                        put("isPayoutEvent", h.isPayoutEvent)
                        put("payoutRecipientName", h.payoutRecipientName)
                        put("receiptNumber", h.receiptNumber)
                        put("escrowStatus", h.escrowStatus)
                        put("approvedByName", h.approvedByName)
                        put("maskedAccountNumber", h.maskedAccountNumber)
                    })
                }
                put("roundHistory", histArr)
                val auditArr = JSONArray()
                g.auditLog.forEach { a ->
                    auditArr.put(JSONObject().apply {
                        put("id", a.id)
                        put("paidByName", a.paidByName)
                        put("amount", a.amount)
                        put("currency", a.currency)
                        put("paidAtLabel", a.paidAtLabel)
                        put("escrowInLabel", a.escrowInLabel)
                        put("approvedByAdmin", a.approvedByAdmin)
                        put("releasedToName", a.releasedToName)
                        put("releasedAtLabel", a.releasedAtLabel)
                        put("receiptNumber", a.receiptNumber)
                    })
                }
                put("auditLog", auditArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun parseContributions(json: String): List<ContributionGroup>? {
        if (json.isBlank()) return null
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val mArr = o.optJSONArray("members") ?: JSONArray()
                    val members = buildList {
                        for (j in 0 until mArr.length()) {
                            val mo = mArr.getJSONObject(j)
                            add(
                                ContributionMember(
                                    uid = mo.optString("uid"),
                                    name = mo.optString("name"),
                                    handle = mo.optString("handle"),
                                    photoUri = mo.optString("photoUri"),
                                    turnOrder = mo.optInt("turnOrder", j + 1),
                                    role = mo.optString("role", "MEMBER"),
                                    trustScore = mo.optInt("trustScore", 85),
                                    paidCurrentRound = mo.optBoolean("paidCurrentRound", false),
                                    hasCollected = mo.optBoolean("hasCollected", false),
                                    paidRoundsCount = mo.optInt("paidRoundsCount", 0),
                                    lateCount = mo.optInt("lateCount", 0),
                                    missedCount = mo.optInt("missedCount", 0),
                                    joinedDateLabel = mo.optString("joinedDateLabel", "Nov 2025"),
                                    paymentApprovalStatus = mo.optString("paymentApprovalStatus", if (mo.optBoolean("paidCurrentRound", false)) "APPROVED" else "UNPAID"),
                                    paymentSubmittedAtMillis = mo.optLong("paymentSubmittedAtMillis", 0L),
                                    paymentProofRef = mo.optString("paymentProofRef"),
                                    paymentProofUri = mo.optString("paymentProofUri"),
                                    approvedByAdminName = mo.optString("approvedByAdminName"),
                                    approvedAtLabel = mo.optString("approvedAtLabel"),
                                    rejectionReason = mo.optString("rejectionReason"),
                                    collectionStatus = mo.optString("collectionStatus", if (mo.optBoolean("hasCollected", false)) "COLLECTED" else "WAITING"),
                                    collectionRequestedAtMillis = mo.optLong("collectionRequestedAtMillis", 0L),
                                    collectedAtLabel = mo.optString("collectedAtLabel"),
                                    collectedApprovedBy = mo.optString("collectedApprovedBy")
                                )
                            )
                        }
                    }
                    val hArr = o.optJSONArray("roundHistory") ?: JSONArray()
                    val history = buildList {
                        for (j in 0 until hArr.length()) {
                            val ho = hArr.getJSONObject(j)
                            add(
                                ContributionRoundHistory(
                                    id = ho.optString("id"),
                                    roundNumber = ho.optInt("roundNumber", 1),
                                    payerUid = ho.optString("payerUid"),
                                    payerName = ho.optString("payerName"),
                                    amount = ho.optDouble("amount", 0.0),
                                    lateFee = ho.optDouble("lateFee", 0.0),
                                    currency = ho.optString("currency", "₦"),
                                    paymentMethod = ho.optString("paymentMethod", "PGLOB Balance"),
                                    paidAtLabel = ho.optString("paidAtLabel", "Today"),
                                    wasOnTime = ho.optBoolean("wasOnTime", true),
                                    isPayoutEvent = ho.optBoolean("isPayoutEvent", false),
                                    payoutRecipientName = ho.optString("payoutRecipientName"),
                                    receiptNumber = ho.optString("receiptNumber"),
                                    escrowStatus = ho.optString("escrowStatus", "IN_ESCROW"),
                                    approvedByName = ho.optString("approvedByName"),
                                    maskedAccountNumber = ho.optString("maskedAccountNumber")
                                )
                            )
                        }
                    }
                    val auditArr = o.optJSONArray("auditLog") ?: JSONArray()
                    val auditEntries = buildList {
                        for (j in 0 until auditArr.length()) {
                            val ao = auditArr.getJSONObject(j)
                            add(
                                ContributionAuditEntry(
                                    id = ao.optString("id"),
                                    paidByName = ao.optString("paidByName"),
                                    amount = ao.optDouble("amount", 0.0),
                                    currency = ao.optString("currency", "₦"),
                                    paidAtLabel = ao.optString("paidAtLabel"),
                                    escrowInLabel = ao.optString("escrowInLabel"),
                                    approvedByAdmin = ao.optString("approvedByAdmin"),
                                    releasedToName = ao.optString("releasedToName"),
                                    releasedAtLabel = ao.optString("releasedAtLabel"),
                                    receiptNumber = ao.optString("receiptNumber")
                                )
                            )
                        }
                    }
                    val reqArr = o.optJSONArray("pendingJoinRequests") ?: JSONArray()
                    val pendingReqs = buildList {
                        for (j in 0 until reqArr.length()) add(reqArr.optString(j))
                    }
                    val agreedArr = o.optJSONArray("agreedMemberUids") ?: JSONArray()
                    val agreedUids = buildList {
                        for (j in 0 until agreedArr.length()) add(agreedArr.optString(j))
                    }
                    add(
                        ContributionGroup(
                            id = o.optString("id"),
                            name = o.optString("name"),
                            purpose = o.optString("purpose"),
                            type = runCatching { ContributionType.valueOf(o.optString("type")) }.getOrDefault(ContributionType.WEEKLY),
                            amountPerRound = o.optDouble("amountPerRound", 10_000.0),
                            currency = o.optString("currency", "₦"),
                            members = members,
                            maxMembers = o.optInt("maxMembers", members.size.coerceAtLeast(2)),
                            currentRound = o.optInt("currentRound", 1),
                            totalRounds = o.optInt("totalRounds", members.size.coerceAtLeast(1)),
                            totalEscrowBalance = o.optDouble("totalEscrowBalance", 0.0),
                            startDateLabel = o.optString("startDateLabel", "Today"),
                            endDateLabel = o.optString("endDateLabel", "Next Month"),
                            nextDueDateLabel = o.optString("nextDueDateLabel", "Fri"),
                            daysRemaining = o.optInt("daysRemaining", 2),
                            payoutOrderType = runCatching { PayoutOrderType.valueOf(o.optString("payoutOrderType")) }.getOrDefault(PayoutOrderType.ROTATION),
                            status = runCatching { ContributionStatus.valueOf(o.optString("status")) }.getOrDefault(ContributionStatus.ACTIVE),
                            requireLateFee = o.optBoolean("requireLateFee", true),
                            lateFeeAmount = o.optDouble("lateFeeAmount", 500.0),
                            mustPayBeforeTurn = o.optBoolean("mustPayBeforeTurn", true),
                            adminCanRemoveUnpaid = o.optBoolean("adminCanRemoveUnpaid", true),
                            screenshotBlockEnabled = o.optBoolean("screenshotBlockEnabled", true),
                            linkedChatId = o.optString("linkedChatId"),
                            creatorUid = o.optString("creatorUid"),
                            pendingJoinRequests = pendingReqs,
                            roundHistory = history,
                            createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis()),
                            groupPhotoUri = o.optString("groupPhotoUri"),
                            groupIconEmoji = o.optString("groupIconEmoji"),
                            rulesText = o.optString("rulesText", "1. Pay before due date.\n2. Funds stay locked in PGLOB Escrow."),
                            agreedMemberUids = agreedUids,
                            allowSubAdminApproveRegistration = o.optBoolean("allowSubAdminApproveRegistration", false),
                            escrowFrozen = o.optBoolean("escrowFrozen", false),
                            paymentAccountMode = o.optString("paymentAccountMode", "PGLOB_ESCROW"),
                            auditLog = auditEntries
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun encodeAppSubAdmins(list: List<AppSubAdmin>): String {
        val arr = JSONArray()
        list.forEach { sa ->
            arr.put(JSONObject().apply {
                put("uid", sa.uid)
                put("name", sa.name)
                put("email", sa.email)
                put("username", sa.username)
                put("roleLabel", sa.roleLabel)
                put("addedByEmail", sa.addedByEmail)
                put("addedAtLabel", sa.addedAtLabel)
                put("permissions", JSONArray(sa.permissions.map { it.name }))
            })
        }
        return arr.toString()
    }

    fun parseAppSubAdmins(json: String): List<AppSubAdmin>? {
        if (json.isBlank()) return null
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val pArr = o.optJSONArray("permissions") ?: JSONArray()
                    val perms = buildSet {
                        for (j in 0 until pArr.length()) {
                            runCatching { AppAdminPermission.valueOf(pArr.optString(j)) }.getOrNull()?.let { add(it) }
                        }
                    }
                    add(
                        AppSubAdmin(
                            uid = o.optString("uid"),
                            name = o.optString("name"),
                            email = o.optString("email"),
                            username = o.optString("username"),
                            roleLabel = o.optString("roleLabel", "Sub-Admin"),
                            permissions = perms,
                            addedByEmail = o.optString("addedByEmail", SUPER_ADMIN_EMAIL),
                            addedAtLabel = o.optString("addedAtLabel", "Today")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun encodeGroupSubAdmins(list: List<GroupSubAdminPermissions>): String {
        val arr = JSONArray()
        list.forEach { g ->
            arr.put(JSONObject().apply {
                put("groupId", g.groupId)
                put("userUid", g.userUid)
                put("userName", g.userName)
                put("canRemindMembers", g.canRemindMembers)
                put("canAddRemoveMember", g.canAddRemoveMember)
                put("canChangeTurnOrder", g.canChangeTurnOrder)
                put("cannotChangeAmount", g.cannotChangeAmount)
                put("cannotEndGroup", g.cannotEndGroup)
                put("canApproveNewMember", g.canApproveNewMember)
            })
        }
        return arr.toString()
    }

    fun parseGroupSubAdmins(json: String): List<GroupSubAdminPermissions> = try {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    GroupSubAdminPermissions(
                        groupId = o.optString("groupId"),
                        userUid = o.optString("userUid"),
                        userName = o.optString("userName"),
                        canRemindMembers = o.optBoolean("canRemindMembers", true),
                        canAddRemoveMember = o.optBoolean("canAddRemoveMember", true),
                        canChangeTurnOrder = o.optBoolean("canChangeTurnOrder", false),
                        cannotChangeAmount = o.optBoolean("cannotChangeAmount", true),
                        cannotEndGroup = o.optBoolean("cannotEndGroup", true),
                        canApproveNewMember = o.optBoolean("canApproveNewMember", false)
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }

    fun encodeAdminLogs(list: List<AdminActivityLog>): String {
        val arr = JSONArray()
        list.take(80).forEach { l ->
            arr.put(JSONObject().apply {
                put("id", l.id)
                put("actorEmail", l.actorEmail)
                put("actorName", l.actorName)
                put("actorRole", l.actorRole)
                put("actionTitle", l.actionTitle)
                put("details", l.details)
                put("timestampLabel", l.timestampLabel)
            })
        }
        return arr.toString()
    }

    fun parseAdminLogs(json: String): List<AdminActivityLog>? {
        if (json.isBlank()) return null
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        AdminActivityLog(
                            id = o.optString("id"),
                            actorEmail = o.optString("actorEmail"),
                            actorName = o.optString("actorName"),
                            actorRole = o.optString("actorRole"),
                            actionTitle = o.optString("actionTitle"),
                            details = o.optString("details"),
                            timestampLabel = o.optString("timestampLabel")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun encodeBroadcasts(list: List<BroadcastAnnouncement>): String {
        val arr = JSONArray()
        list.take(30).forEach { b ->
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("message", b.message)
                put("targetScope", b.targetScope)
                put("targetGroupName", b.targetGroupName)
                put("sentByName", b.sentByName)
                put("sentAtLabel", b.sentAtLabel)
            })
        }
        return arr.toString()
    }

    fun parseBroadcasts(json: String): List<BroadcastAnnouncement>? {
        if (json.isBlank()) return null
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        BroadcastAnnouncement(
                            id = o.optString("id"),
                            title = o.optString("title"),
                            message = o.optString("message"),
                            targetScope = o.optString("targetScope", "ALL_USERS"),
                            targetGroupName = o.optString("targetGroupName"),
                            sentByName = o.optString("sentByName"),
                            sentAtLabel = o.optString("sentAtLabel")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun encodeAppAdminConfig(c: AppAdminConfig): String = JSONObject().apply {
        put("appName", c.appName)
        put("logoVariant", c.logoVariant)
        put("transferFeePercent", c.transferFeePercent)
        put("defaultLateFee", c.defaultLateFee)
        put("defaultCurrency", c.defaultCurrency)
    }.toString()

    fun parseAppAdminConfig(json: String): AppAdminConfig {
        if (json.isBlank()) return AppAdminConfig()
        return try {
            val o = JSONObject(json)
            AppAdminConfig(
                appName = o.optString("appName", "PGLOB APP"),
                logoVariant = o.optString("logoVariant", "PGLOB Green + White"),
                transferFeePercent = o.optDouble("transferFeePercent", 0.5),
                defaultLateFee = o.optDouble("defaultLateFee", 500.0),
                defaultCurrency = o.optString("defaultCurrency", "₦")
            )
        } catch (_: Exception) {
            AppAdminConfig()
        }
    }

    fun encodeStatusTrackers(list: List<StatusInteractionTracker>): String {
        val arr = JSONArray()
        list.take(100).forEach { t ->
            arr.put(JSONObject().apply {
                put("statusId", t.statusId)
                put("userName", t.userName)
                put("userHandle", t.userHandle)
                put("photoUri", t.photoUri)
                put("interactionType", t.interactionType)
                put("detailText", t.detailText)
                put("timeLabel", t.timeLabel)
            })
        }
        return arr.toString()
    }

    fun parseStatusTrackers(json: String): List<StatusInteractionTracker> = try {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    StatusInteractionTracker(
                        statusId = o.optString("statusId"),
                        userName = o.optString("userName"),
                        userHandle = o.optString("userHandle"),
                        photoUri = o.optString("photoUri"),
                        interactionType = o.optString("interactionType"),
                        detailText = o.optString("detailText"),
                        timeLabel = o.optString("timeLabel", "Just now")
                    )
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }

    /**
     * Pure computation helper for Trust Score updates (+5 on time, -10 late, -20 missed).
     */
    fun nextTrustScore(current: Int, onTime: Boolean, missed: Boolean = false): Int {
        val delta = when {
            missed -> -20
            onTime -> 5
            else -> -10
        }
        return (current + delta).coerceIn(0, 100)
    }

    fun formatRemaining48h(submittedAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (submittedAtMillis <= 0L) return "48h 00m left"
        val elapsed = (nowMillis - submittedAtMillis).coerceAtLeast(0L)
        val remaining = AUTO_APPROVE_WINDOW_MS - elapsed
        if (remaining <= 0L) return "48h passed • Auto-Approve ready"
        val totalMinutes = remaining / 60_000L
        val hours = totalMinutes / 60L
        val mins = totalMinutes % 60L
        return "${hours}h ${mins}m left"
    }

    /**
     * Pure evaluator for the 48-hour Escrow Auto-Approve rule:
     * 1) Pay proofs waiting >= 48h:
     *    - Wallet / PGLOB Balance proofs -> AUTO_APPROVED ("Auto-Approved by PGLOB"), credited to escrow.
     *    - External Bank/OPay proofs without matching "PGLOB" reference -> UNDER_REVIEW for Owner oversight.
     * 2) Collect requests waiting >= 48h when it's member's turn & pot funded -> AUTO_COLLECTED, released to member.
     */
    fun evaluateAutoApprove48h(
        groups: List<ContributionGroup>,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<List<ContributionGroup>, List<String>> {
        val events = mutableListOf<String>()
        val nowLabel = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(Date(nowMillis))
        val updatedGroups = groups.map { group ->
            if (group.escrowFrozen || group.status != ContributionStatus.ACTIVE) return@map group

            var nextEscrow = group.totalEscrowBalance
            var nextHistory = group.roundHistory
            var nextAudit = group.auditLog
            var changed = false

            var updatedMembers = group.members.map { member ->
                if (member.paymentApprovalStatus == "WAITING_APPROVAL" &&
                    member.paymentSubmittedAtMillis > 0L &&
                    (nowMillis - member.paymentSubmittedAtMillis) >= AUTO_APPROVE_WINDOW_MS
                ) {
                    changed = true
                    val isWalletProof = member.paymentProofRef.contains("Wallet", ignoreCase = true) ||
                        member.paymentProofRef.contains("PGLOB Balance", ignoreCase = true) ||
                        member.paymentProofRef.contains("PGLOB", ignoreCase = true)
                    if (isWalletProof) {
                        val receiptNo = "PG-AUTO-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
                        nextEscrow += group.amountPerRound
                        nextHistory = listOf(
                            ContributionRoundHistory(
                                id = "auto_${UUID.randomUUID().toString().take(8)}",
                                roundNumber = group.currentRound,
                                payerUid = member.uid,
                                payerName = member.name,
                                amount = group.amountPerRound,
                                currency = group.currency,
                                paymentMethod = "PGLOB Auto-Approve (48h)",
                                paidAtLabel = nowLabel,
                                wasOnTime = true,
                                receiptNumber = receiptNo,
                                escrowStatus = "IN_ESCROW",
                                approvedByName = "Auto-Approved by PGLOB",
                                maskedAccountNumber = "9000***480"
                            )
                        ) + nextHistory
                        nextAudit = listOf(
                            ContributionAuditEntry(
                                id = "aud_${UUID.randomUUID().toString().take(8)}",
                                paidByName = member.name,
                                amount = group.amountPerRound,
                                currency = group.currency,
                                paidAtLabel = nowLabel,
                                escrowInLabel = "PGLOB Escrow Locked",
                                approvedByAdmin = "Auto-Approved by PGLOB (48h)",
                                receiptNumber = receiptNo
                            )
                        ) + nextAudit
                        events += "Auto-Approved ${member.name}'s payment in ${group.name} after 48h"
                        member.copy(
                            paidCurrentRound = true,
                            paidRoundsCount = member.paidRoundsCount + 1,
                            trustScore = nextTrustScore(member.trustScore, onTime = true),
                            paymentApprovalStatus = "AUTO_APPROVED",
                            approvedByAdminName = "Auto-Approved by PGLOB",
                            approvedAtLabel = nowLabel
                        )
                    } else {
                        events += "${member.name}'s external proof in ${group.name} flagged Under PGLOB Review (48h)"
                        member.copy(paymentApprovalStatus = "UNDER_REVIEW")
                    }
                } else {
                    member
                }
            }

            // Evaluate 48h auto-collection for turn member if all paid and waiting >= 48h
            val allPaid = updatedMembers.isNotEmpty() && updatedMembers.all { it.paidCurrentRound }
            val currentTurnUid = group.currentTurnMember?.uid
            var nextRound = group.currentRound
            var nextStatus = group.status

            updatedMembers = updatedMembers.map { member ->
                if (member.uid == currentTurnUid &&
                    member.collectionStatus == "REQUESTED" &&
                    member.collectionRequestedAtMillis > 0L &&
                    (nowMillis - member.collectionRequestedAtMillis) >= AUTO_APPROVE_WINDOW_MS &&
                    allPaid
                ) {
                    changed = true
                    val pot = group.totalPotPerRound
                    nextEscrow = (nextEscrow - pot).coerceAtLeast(0.0)
                    val receiptNo = "PG-REL-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}"
                    nextHistory = listOf(
                        ContributionRoundHistory(
                            id = "rel_${UUID.randomUUID().toString().take(8)}",
                            roundNumber = group.currentRound,
                            payerUid = member.uid,
                            payerName = member.name,
                            amount = pot,
                            currency = group.currency,
                            paymentMethod = "PGLOB 48h Auto-Release",
                            paidAtLabel = nowLabel,
                            wasOnTime = true,
                            isPayoutEvent = true,
                            payoutRecipientName = member.name,
                            receiptNumber = receiptNo,
                            escrowStatus = "RELEASED_TO_MEMBER",
                            approvedByName = "Auto-Approved by PGLOB",
                            maskedAccountNumber = "9000***480"
                        )
                    ) + nextHistory
                    nextAudit = listOf(
                        ContributionAuditEntry(
                            id = "audr_${UUID.randomUUID().toString().take(8)}",
                            paidByName = "${group.name} Pool",
                            amount = pot,
                            currency = group.currency,
                            paidAtLabel = nowLabel,
                            escrowInLabel = "Released after 48h",
                            approvedByAdmin = "Auto-Approved by PGLOB",
                            releasedToName = member.name,
                            releasedAtLabel = nowLabel,
                            receiptNumber = receiptNo
                        )
                    ) + nextAudit
                    events += "Auto-Released ${group.currency}${pot.toInt()} to ${member.name} after 48h"
                    val isLast = group.currentRound >= updatedMembers.size
                    nextRound = if (isLast) group.currentRound else group.currentRound + 1
                    nextStatus = if (isLast) ContributionStatus.COMPLETED else ContributionStatus.ACTIVE
                    member.copy(
                        hasCollected = true,
                        collectionStatus = "AUTO_COLLECTED",
                        collectedAtLabel = nowLabel,
                        collectedApprovedBy = "Auto-Approved by PGLOB"
                    )
                } else {
                    member
                }
            }

            if (!changed) group else {
                val resetPaid = nextRound != group.currentRound
                val finalMembers = if (resetPaid) {
                    updatedMembers.map { m ->
                        m.copy(
                            paidCurrentRound = false,
                            paymentApprovalStatus = "UNPAID",
                            paymentSubmittedAtMillis = 0L,
                            paymentProofRef = ""
                        )
                    }
                } else updatedMembers
                group.copy(
                    members = finalMembers,
                    totalEscrowBalance = nextEscrow,
                    currentRound = nextRound,
                    status = nextStatus,
                    roundHistory = nextHistory,
                    auditLog = nextAudit
                )
            }
        }
        return updatedGroups to events
    }
}
