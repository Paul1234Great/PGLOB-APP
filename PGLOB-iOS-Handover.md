# PGLOB — iOS & Flutter Cross-Platform Handover Specification

> **Purpose**: Complete technical blueprint for rebuilding **PGLOB** in **Flutter (Dart)** on macOS/Xcode for **iOS IPA / TestFlight / App Store** and **Android APK / AAB** with 100% feature parity, identical PGLOB Black Edition design, and shared Firebase backend so iPhone and Android users communicate seamlessly.

---

## 1. Brand Identity & Design System (`PGLOB Black Edition`)

- **App Name**: `PGLOB` (App Title in headers: `PGLOB APP`)
- **Brand Policy**: Strictly **PGLOB** everywhere — zero third-party messaging brand names anywhere in UI, code, splash, welcome, store metadata, or footers.
- **Footer**: `PGLOB APP v15 • PGLOB Black Edition`
- **Official Logo (`PglobBrandLogo`)**:
  - Green Circle (`#00A884`, launcher adaptive background `#25D366`)
  - Crisp White speech-bubble outline with bottom-left tail
  - Bold White `"P"` centered inside
- **Color Palette (Dark-First High Contrast)**:
  - Primary Background (`WaBackground`): `#0B141A`
  - Card / Sheet Surface (`WaCard`): `#1A2B34`
  - Input Pill / Secondary Surface (`WaInput` / `WaDivider`): `#2A3942`
  - Primary Text (`WaTextPrimary`): `#FFFFFF` (Bold/SemiBold, readable `16sp+` body/titles)
  - Secondary Text (`WaTextSecondary`): `#C0C9D1`
  - Muted Caption / Splash Footer: `#8696A0`
  - Brand Green Accent (`WaGreen`): `#00A884`
  - Sent Chat Bubble (`WaMyBubble`): `#005C4B`
  - Received Chat Bubble (`WaOtherBubble`): `#2A3942`
  - Gold Accent (`WaGold`): `#FFD700`
  - Read Tick (`WaBlueTick`): `#53BDEB`
  - Destructive / Alert (`WaRed`): `#FF3B30`
  - Pure Black (`WaPureBlack`): `#000000`
- **Screen Adaptation Rules (iPhone & Android)**:
  - Full-screen edge-to-edge layout (`SafeArea` in Flutter, `enableEdgeToEdge()` + `statusBarsPadding()` / `navigationBarsPadding()` in Compose)
  - Never hardcode device dimensions (works from iPhone SE / Android 4.5" up to iPhone Pro Max / iPad / Foldables)
  - Minimum `48dp` interactive touch targets and `16sp+` crisp legible typography with zero blur filters.

---

## 2. App Launch, Splash & Auth Flows

1. **Splash Screen (2 Seconds Exact)**:
   - Solid `#0B141A` full screen, non-interactive (no skip/tap).
   - Center: `120dp` Green Circle `#00A884` + White P Bubble logo smoothly scaling `0.9 -> 1.0`.
   - Bottom Center: Small PGLOB mark (`20dp`) + `"from PGLOB"` (`14sp`, `#8696A0`).
   - **Preload Rule**: While Splash displays (2s), returning users load local session + Firestore/Room chats in the background so Home opens immediately with zero blank screen.
2. **Routing After 2s Splash**:
   - **Flow A — New User**: Routes to `WelcomeAuthScreen` -> `"Welcome to PGLOB"`, `"Chat • Pay • Earn • Ajo • Market - All in One App"`, country/language quick pills, clickable `"By continuing, you agree to our Privacy Policy and Terms"`, full-width `56dp` pill CTA **`AGREE AND CONTINUE`** (`#00A884`), plus Secondary Sign-In and optional Google Sign-In.
   - **Flow B — Returning User (`stayLoggedIn = true`)**: Routes straight to **Chats List** (`Stay Login Forever` using persisted Firebase/DataStore session).
3. **Sign Up & Verification**:
   - Country selector (Nigeria, Ghana, Kenya, UK, USA, India, Brazil, etc. — maps dial code, flag, currency, and payment methods)
   - Phone number validation (`SecurityAndNumberCodec`), optional Email, Password + Strength indicator, Confirm Password, Terms checkbox -> OTP Verification screen -> `SmartOnboardingSetupScreen` (unique `@username` availability check, 4-digit Transaction PIN + Face ID / Fingerprint setup).
4. **App Lock & Secret Vault**:
   - Optional App Lock (`BiometricPrompt` on Android / `local_auth` Face ID & Touch ID on iOS) + timeout + Secret Encrypted Vault PIN.

---

## 3. Complete Feature & Screen Inventory

### Navigation Architecture
- **Bottom Navigation Tabs (4)**:
  1. `Chats` — Top Super-App sub-tabs: `Chats | Earn | Live | Connect | Market | Reels | Games | Learn | Jobs | Pay | Dating`
  2. `Updates` — Status Notes strip + 24h Status Updates + Channels
  3. `Communities` — Communities & Announcements
  4. `Calls` — Audio & Video Call Logs
- **Top Header Bar**: `"PGLOB"` (`24sp` Bold White) + PGLOB emblem, Notification Bell (unread badge), Camera button, 3-dot Menu (New Group, New Ajo Contribution, Locked Chats, Chat Wallpaper, Settings), Search pill with `Ask AI` chip.

### Core Modules & Features
1. **Messaging (`HomeAndChatScreens`)**:
   - Filter pills (`All | Unread | Favorites | Groups | Locked`), Archived chats folder, long-press actions (Pin, Favorite, Lock, Mute, Archive, Delete).
   - 1-on-1 & Group Chats: End-to-end encrypted indicator, custom wallpaper (`Solid`, `Doodle`, `OLED`, custom gallery photo), text, voice notes, photo/camera, document/location/contact/poll/event sharing, reply/forward/react/star/delete, in-chat `Send Money`, `Send Gift`, `PGLOB AI` assistant & multi-language `Translate`, audio/video call overlay.
   - Offline Queue (`Room` / SQLite cache + `PendingMessageEntity`): Auto-syncs when network reconnects.
2. **Status Notes (`StatusNoteScreens` + `StatusNoteCodec`)**:
   - Horizontal strip above Chats & Updates (`Your Note: + Add Note`, expired state `"Your Note don expire"`, friend notes).
   - Write/Edit/Replace Note (`100` chars max with `0/100` counter, 5 background presets Green/Black/Gold/Blue/Pink, emoji bar, optional mood & music, privacy `Everyone / Contacts Only / Ajo Group / Custom`, `24h Free` or `48h PRO`).
   - Full-Screen Note Viewer: swipe left/right between notes, swipe down to close, real heart-burst Likes (0 seeded fake likes), Seen By & Likers bottom sheets, 4-option Reshare (to My Note, to My Status, to Chat/Ajo Group, External Share / Copy Link), Reply to DM, Edit keeping same Note ID/Likes, Replace, Delete, Report/Block, tapping author opens `PublicUserProfileSheet`.
3. **24h Status & Reels (`UpdatesCommunitiesCallsScreens` + `MarketReelsGamesLearnJobsScreens`)**:
   - Multi-photo/text Status updates, reactions, viewer list, reply/forward into chat (`StatusQuoteCodec`).
   - Short vertical Reels feed with likes, views, hashtags, music labels, and Safety Report/Block.
4. **Group Contribution / Ajo / Esusu Escrow (`ContributionScreens` + `ContributionAdminCodec`)**:
   - Create Daily/Weekly/Monthly savings circles linked to group chats (`ContributionCodec`).
   - Turn order payout rotation, **PGLOB Escrow Hold**, proof submission & approval, **48-Hour Auto-Approve (`AUTO_APPROVED` for wallet / `AUTO_COLLECTED` payout)** rule engine, printable/shareable receipts, Sub-Admin role locks (`cannotChangeAmount`, `cannotEndGroup`, `canApproveNewMember`).
5. **PGLOB Pay / Wallet / Earn (`WalletPayEarnScreens` + `CountryPayCatalog`)**:
   - 10-digit PGLOB virtual account (`SecurityAndNumberCodec`), country-localized banks/wallets (OPay, PalmPay, M-Pesa, Monzo, CashApp, etc.), Send/Add/Withdraw/Airtime/Data/Bills with 4-digit PIN & Biometric gate, transaction cooldown locks, Daily Check-In Coins, VIP Tiers (`FREE`, `GOLD`, `DIAMOND`), Referral system.
6. **Marketplace & Order History (`MarketReelsGamesLearnJobsScreens` + `OrderHistoryScreen`)**:
   - Browse & sell products, zero-blank full Product Detail + Sticky Checkout (`WALLET`, `COINS`, Cash on Delivery with PGLOB Escrow), delivery address & seller chat, and **Order History** tracking (`Pending`, `Escrowed`, `Delivered` confirmation).
7. **User Profile System (`UserProfileComponents` + `ProfileSettingsAdminScreens` + `ProfileCodec`)**:
   - Upload/camera/remove Profile Picture (compressed JPEG Data URL synced to `publicProfiles`), Short Bio (`0/200`), Interests tags + quick suggestions (`0/10`), visible across Connect, New Chat Global Members, 1-on-1 Chat Info, and Status Note Viewer via `PublicUserProfileSheet`.
8. **Admin & Sub-Admin Dashboard (`AdminSubAdminScreens`)**:
   - Super-Admin (`paulodogun197@gmail.com`) + delegated App Sub-Admins with granular `AppAdminPermission` flags, User Moderation (ban/unban, verify tick), Contribution Disputes, Safety Reports queue, Marketplace/Jobs approval.
9. **Connect, Dating, Live Audio/Video Rooms, Games, Learn, Jobs**:
   - Connect Globally (country filters, random chat, profile cards), Dating 18+ Discover/Matches/Swipes, Live Rooms (`AUDIO` / `VIDEO` + AI translate), Coin Mini-Games, Learn courses, Global Jobs board.
10. **PGLOB AI Assistant**:
   - Powered by Firebase AI Logic (`Firebase.ai`, Gemini) for in-chat smart replies, translation, summarization, and onboarding assistance.

---

## 4. Shared Firebase Backend & Security Rules (`firestore.rules`)

All collections enforce Firebase Authentication (`request.auth != null`) and field-level validation:
- `/users/{userId}` — Private PII, wallet balance, PIN, bio, interests, photoUri (Owner or Admin only)
- `/publicProfiles/{userId}` — Non-PII public profile (`displayName`, `username`, `bio`, `interests`, `photoUri`, `country`, `vipTier`, `isOnline`, `datingEnabled`, `isPublic`)
- `/usernames/{username}` — Unique username reservation registry
- `/chats/{chatId}` & `/chats/{chatId}/messages/{messageId}` — Member-isolated (`request.auth.uid in memberUids`)
- `/transactions/{txId}` — Pay, Escrow, Market & Wallet ledger (Owner, Counterparty, or Admin)
- `/marketProducts/{productId}`, `/statuses/{statusId}`, `/reels/{reelId}`, `/jobs/{jobId}`, `/liveRooms/{roomId}` — Public read, owner-scoped write/delete
- `/datingSwipes/{swipeId}`, `/datingMatches/{matchId}` — Sender/member isolated
- `/reports/{reportId}`, `/admins/{adminUid}` — Admin moderation & safety reporting

---

## 5. Flutter iOS + Android Rebuild Mapping

| Android (Kotlin / Jetpack Compose) | Flutter (Dart) Equivalent |
| :--- | :--- |
| `androidx.compose` Material 3 UI | Flutter Material 3 (`useMaterial3: true`, `SafeArea`) |
| `ViewModel` + `StateFlow` | `Riverpod` (`StateNotifierProvider` / `StreamProvider`) |
| Custom screen/backstack + `BackHandler` | `GoRouter` (with deep link `https://pglob.app/...`) |
| `Firebase.auth` + `CredentialManager` | `firebase_auth` + `google_sign_in` + `sign_in_with_apple` |
| `Firebase.firestore` | `cloud_firestore` |
| `Firebase.ai` (AI Logic) | `firebase_ai` / `firebase_vertexai` |
| `Room` local offline cache + queue | `drift` or `sqflite` |
| `DataStore` preferences | `shared_preferences` / `flutter_secure_storage` |
| `BiometricPrompt` | `local_auth` (Face ID / Touch ID / Biometrics) |
| `ActivityResultContracts.PickVisualMedia` | `image_picker` (PHPicker on iOS / Photo Picker on Android) |
| Build Targets | `flutter build apk --release` & `flutter build ipa --release` (requires Xcode on macOS + Apple Developer Push APNs + FCM) |
