package com.example.data.model

enum class ContributionType(val label: String, val cycleDays: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    CUSTOM("Custom", 14)
}

enum class PayoutOrderType(val label: String) {
    ROTATION("Rotation Order"),
    RANDOM("Random Draw"),
    ADMIN_CHOOSES("Admin Chooses"),
    FIRST_COME("First Come First Serve")
}

enum class ContributionStatus(val label: String) {
    ACTIVE("Active"),
    PAUSED("Paused"),
    COMPLETED("Completed")
}

data class ContributionMember(
    val uid: String,
    val name: String,
    val handle: String,
    val photoUri: String = "",
    val turnOrder: Int,
    val role: String = "MEMBER", // ADMIN, SUB_ADMIN, MEMBER
    val trustScore: Int = 88,
    val paidCurrentRound: Boolean = false,
    val hasCollected: Boolean = false,
    val paidRoundsCount: Int = 0,
    val lateCount: Int = 0,
    val missedCount: Int = 0,
    val joinedDateLabel: String = "Nov 2025",
    // Pay & Collect Escrow approval tracking
    val paymentApprovalStatus: String = "UNPAID", // UNPAID, WAITING_APPROVAL, APPROVED, AUTO_APPROVED, REJECTED, UNDER_REVIEW
    val paymentSubmittedAtMillis: Long = 0L,
    val paymentProofRef: String = "",
    val paymentProofUri: String = "",
    val approvedByAdminName: String = "",
    val approvedAtLabel: String = "",
    val rejectionReason: String = "",
    val collectionStatus: String = "WAITING", // WAITING, REQUESTED, COLLECTED, AUTO_COLLECTED
    val collectionRequestedAtMillis: Long = 0L,
    val collectedAtLabel: String = "",
    val collectedApprovedBy: String = ""
)

data class ContributionRoundHistory(
    val id: String,
    val roundNumber: Int,
    val payerUid: String,
    val payerName: String,
    val amount: Double,
    val lateFee: Double = 0.0,
    val currency: String = "₦",
    val paymentMethod: String = "PGLOB Balance",
    val paidAtLabel: String,
    val wasOnTime: Boolean = true,
    val isPayoutEvent: Boolean = false,
    val payoutRecipientName: String = "",
    val receiptNumber: String = "",
    val escrowStatus: String = "IN_ESCROW", // IN_ESCROW, RELEASED_TO_MEMBER, REFUNDED
    val approvedByName: String = "",
    val maskedAccountNumber: String = ""
)

data class ContributionAuditEntry(
    val id: String,
    val paidByName: String,
    val amount: Double,
    val currency: String = "₦",
    val paidAtLabel: String,
    val escrowInLabel: String,
    val approvedByAdmin: String,
    val releasedToName: String = "",
    val releasedAtLabel: String = "",
    val receiptNumber: String
)

data class ContributionGroup(
    val id: String,
    val name: String,
    val purpose: String,
    val type: ContributionType = ContributionType.WEEKLY,
    val amountPerRound: Double,
    val currency: String = "₦",
    val members: List<ContributionMember> = emptyList(),
    val maxMembers: Int = 10,
    val currentRound: Int = 1,
    val totalRounds: Int = 10,
    val totalEscrowBalance: Double = 0.0,
    val startDateLabel: String = "Fri 14 Nov",
    val endDateLabel: String = "Fri 16 Jan",
    val nextDueDateLabel: String = "Fri 21 Nov",
    val daysRemaining: Int = 2,
    val payoutOrderType: PayoutOrderType = PayoutOrderType.ROTATION,
    val status: ContributionStatus = ContributionStatus.ACTIVE,
    val requireLateFee: Boolean = true,
    val lateFeeAmount: Double = 500.0,
    val mustPayBeforeTurn: Boolean = true,
    val adminCanRemoveUnpaid: Boolean = true,
    val screenshotBlockEnabled: Boolean = true,
    val linkedChatId: String = "",
    val creatorUid: String = "",
    val pendingJoinRequests: List<String> = emptyList(),
    val roundHistory: List<ContributionRoundHistory> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis(),
    // Group Picture, Rules, Escrow Lock & Audit Trail
    val groupPhotoUri: String = "",
    val groupIconEmoji: String = "",
    val rulesText: String = "1. Pay before Friday 6:00 PM each cycle.\n2. Late payment attracts the agreed late fee into Escrow.\n3. All funds remain locked inside PGLOB Escrow until release.",
    val agreedMemberUids: List<String> = emptyList(),
    val allowSubAdminApproveRegistration: Boolean = false,
    val escrowFrozen: Boolean = false,
    val paymentAccountMode: String = "PGLOB_ESCROW", // PGLOB_ESCROW, LEGACY_ADMIN_PERSONAL
    val auditLog: List<ContributionAuditEntry> = emptyList()
) {
    val totalPotPerRound: Double
        get() = amountPerRound * members.size.coerceAtLeast(1)

    val paidMembersCount: Int
        get() = members.count { it.paidCurrentRound }

    val currentTurnMember: ContributionMember?
        get() = members.firstOrNull { it.turnOrder == currentRound }
            ?: members.firstOrNull { !it.hasCollected }
            ?: members.firstOrNull()
}

data class GroupSubAdminPermissions(
    val groupId: String,
    val userUid: String,
    val userName: String,
    val canRemindMembers: Boolean = true,
    val canAddRemoveMember: Boolean = true,
    val canChangeTurnOrder: Boolean = false,
    val cannotChangeAmount: Boolean = true,
    val cannotEndGroup: Boolean = true,
    val canApproveNewMember: Boolean = false
)

enum class AppAdminPermission(val label: String, val description: String) {
    VIEW_DASHBOARD("View Dashboard Stats", "Access KPI metrics & live platform analytics"),
    MANAGE_USERS("Manage Users", "Search users, inspect trust scores, block/unblock"),
    MANAGE_CONTRIBUTIONS("Manage Contribution Groups", "Pause, resume, inspect or end contribution circles"),
    APPROVE_PAYOUTS("Approve / Hold Payouts", "Force payouts, verify holds, issue refunds"),
    VIEW_ESCROW_BALANCE("View Escrow Balance", "See total funds locked in PGLOB Escrow"),
    HANDLE_REPORTS("Handle Reports & Disputes", "Warn, block, or resolve reported incidents"),
    SEND_BROADCAST("Send Broadcast Announcements", "Send global or group-wide announcements"),
    MANAGE_SUB_ADMINS("Add / Remove Sub-Admins", "Super Admin exclusive sub-admin management"),
    EDIT_APP_SETTINGS("Edit App Settings", "Adjust fee %, default late fee & currency"),
    VIEW_TRANSACTIONS("View All Transactions", "Audit payments, escrow logs & receipts"),
    VERIFY_BADGES("Verify Badges", "Grant verified & VIP badges to trusted members")
}

data class AppSubAdmin(
    val uid: String,
    val name: String,
    val email: String,
    val username: String,
    val roleLabel: String = "Sub-Admin",
    val permissions: Set<AppAdminPermission> = emptySet(),
    val addedByEmail: String = "paulodogun197@gmail.com",
    val addedAtLabel: String = "Today"
)

data class AdminActivityLog(
    val id: String,
    val actorEmail: String,
    val actorName: String,
    val actorRole: String,
    val actionTitle: String,
    val details: String,
    val timestampLabel: String
)

data class BroadcastAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val targetScope: String, // ALL_USERS, CONTRIBUTION_MEMBERS, SPECIFIC_GROUP
    val targetGroupName: String = "",
    val sentByName: String,
    val sentAtLabel: String
)

data class AppAdminConfig(
    val appName: String = "PGLOB APP",
    val logoVariant: String = "PGLOB Green + White",
    val transferFeePercent: Double = 0.5,
    val defaultLateFee: Double = 500.0,
    val defaultCurrency: String = "₦"
)

data class StatusInteractionTracker(
    val statusId: String,
    val userName: String,
    val userHandle: String,
    val photoUri: String = "",
    val interactionType: String, // LIKED, REPLIED, RESHARED, VIEWED
    val detailText: String = "",
    val timeLabel: String = "Just now"
)
