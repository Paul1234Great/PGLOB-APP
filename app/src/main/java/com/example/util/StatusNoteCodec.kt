package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.data.model.StatusNote
import com.example.data.model.StatusNoteActor
import com.google.firebase.Timestamp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

data class StatusNoteBgPreset(
    val hex: String,
    val label: String,
    val bgColor: Color,
    val contentColor: Color
)

object StatusNoteCodec {
    const val MAX_NOTE_LENGTH = 100

    val BG_PRESETS = listOf(
        StatusNoteBgPreset("#00A884", "Green", Color(0xFF00A884), Color(0xFFFFFFFF)),
        StatusNoteBgPreset("#0B141A", "Black", Color(0xFF0B141A), Color(0xFFFFFFFF)),
        StatusNoteBgPreset("#FFD700", "Gold", Color(0xFFFFD700), Color(0xFF0B141A)),
        StatusNoteBgPreset("#1E88E5", "Blue", Color(0xFF1E88E5), Color(0xFFFFFFFF)),
        StatusNoteBgPreset("#EC4899", "Pink", Color(0xFFEC4899), Color(0xFFFFFFFF))
    )

    val PRIVACY_OPTIONS = listOf(
        "EVERYONE" to "Everyone",
        "CONTACTS" to "My Contacts Only",
        "AJO_GROUP" to "Only Ajo Group",
        "CUSTOM" to "Custom"
    )

    val MOOD_OPTIONS = listOf("😊 Happy", "😔 Sad", "🔥 Hot")

    val MUSIC_PRESETS = listOf(
        "Afrobeats Mix 🎵",
        "Amapiano Vibes 🎶",
        "Lofi Chill 🎧",
        "Gospel & Praise 🙏",
        "Street Hip-Hop 🔥"
    )

    val QUICK_EMOJIS = listOf(
        "🔥", "❤️", "😊", "😔", "😂", "🙌", "💰", "🎉",
        "👑", "✈️", "🙏", "💯", "😍", "🎶", "⚡", "💚"
    )

    fun presetForHex(hex: String): StatusNoteBgPreset =
        BG_PRESETS.firstOrNull { it.hex.equals(hex, ignoreCase = true) } ?: BG_PRESETS.first()

    fun clampNoteText(raw: String): String = raw.take(MAX_NOTE_LENGTH)

    fun formatCharCounter(text: String): String = "${text.take(MAX_NOTE_LENGTH).length}/$MAX_NOTE_LENGTH"

    fun noteShareUrl(noteId: String): String {
        val clean = noteId.trim().ifBlank { "live" }
        return "https://pglob.app/note/$clean"
    }

    fun formatRelativeTime(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (timestampMillis <= 0L) return "Just now"
        val diff = (nowMillis - timestampMillis).coerceAtLeast(0L)
        val mins = diff / 60_000L
        val hours = diff / 3_600_000L
        val days = diff / 86_400_000L
        return when {
            mins < 1L -> "Just now"
            mins < 60L -> "${mins}m ago"
            hours < 24L -> "${hours}h ago"
            else -> "${days}d ago"
        }
    }

    fun isNoteVisibleToUser(
        note: StatusNote,
        viewerUid: String,
        contactUids: Set<String>,
        ajoMemberUids: Set<String>,
        blockedUids: Set<String>,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (note.text.isBlank()) return false
        if (note.isExpiredAt(nowMillis)) return false
        if (blockedUids.contains(note.userId)) return false
        if (note.resharedFromUid.isNotBlank() && blockedUids.contains(note.resharedFromUid)) return false
        if (note.userId == viewerUid) return true
        return when (note.privacy.uppercase()) {
            "CONTACTS" -> contactUids.contains(note.userId)
            "AJO_GROUP" -> ajoMemberUids.contains(note.userId)
            "CUSTOM" -> note.customAllowedUids.contains(viewerUid)
            else -> true // EVERYONE
        }
    }

    fun createNewNote(
        userId: String,
        authorName: String,
        authorAvatarUri: String,
        text: String,
        bgColor: String,
        music: String,
        mood: String,
        privacy: String,
        customAllowedUids: List<String>,
        durationHours: Int,
        nowMillis: Long = System.currentTimeMillis(),
        noteId: String = "note_${UUID.randomUUID().toString().replace("-", "").take(14)}"
    ): StatusNote {
        val cleanHours = if (durationHours == 48) 48 else 24
        val cleanPrivacy = if (privacy in listOf("EVERYONE", "CONTACTS", "AJO_GROUP", "CUSTOM")) privacy else "EVERYONE"
        val cleanBg = presetForHex(bgColor).hex
        val nowTs = Timestamp(Date(nowMillis))
        val expTs = Timestamp(Date(nowMillis + cleanHours * 3_600_000L))
        return StatusNote(
            id = noteId,
            userId = userId,
            authorName = authorName.ifBlank { "PGLOB Member" }.take(80),
            authorAvatarUri = authorAvatarUri.take(500),
            text = clampNoteText(text.trim()),
            bgColor = cleanBg,
            music = music.trim().take(80),
            mood = mood.trim().take(40),
            privacy = cleanPrivacy,
            isPublic = cleanPrivacy != "CUSTOM",
            customAllowedUids = if (cleanPrivacy == "CUSTOM") customAllowedUids.distinct().take(100) else emptyList(),
            durationHours = cleanHours,
            likes = emptyList(),
            reshares = emptyList(),
            viewers = emptyList(),
            edited = false,
            createdAt = nowTs,
            updatedAt = nowTs,
            expiresAt = expTs
        )
    }

    fun editNoteKeepingSameId(
        existing: StatusNote,
        text: String,
        bgColor: String,
        music: String,
        mood: String,
        privacy: String,
        customAllowedUids: List<String>,
        durationHours: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): StatusNote {
        val cleanHours = if (durationHours == 48) 48 else 24
        val cleanPrivacy = if (privacy in listOf("EVERYONE", "CONTACTS", "AJO_GROUP", "CUSTOM")) privacy else "EVERYONE"
        val cleanBg = presetForHex(bgColor).hex
        val createdMs = existing.createdAtMillisEffective().takeIf { it > 0L } ?: nowMillis
        return existing.copy(
            text = clampNoteText(text.trim()),
            bgColor = cleanBg,
            music = music.trim().take(80),
            mood = mood.trim().take(40),
            privacy = cleanPrivacy,
            isPublic = cleanPrivacy != "CUSTOM",
            customAllowedUids = if (cleanPrivacy == "CUSTOM") customAllowedUids.distinct().take(100) else emptyList(),
            durationHours = cleanHours,
            edited = true,
            updatedAt = Timestamp(Date(nowMillis)),
            expiresAt = Timestamp(Date(createdMs + cleanHours * 3_600_000L))
        )
    }

    fun buildResharedNote(
        original: StatusNote,
        resharerUid: String,
        resharerName: String,
        resharerAvatarUri: String,
        durationHours: Int = 24,
        nowMillis: Long = System.currentTimeMillis(),
        newNoteId: String = "note_${UUID.randomUUID().toString().replace("-", "").take(14)}"
    ): StatusNote {
        val cleanHours = if (durationHours == 48) 48 else 24
        val origAuthor = original.resharedFromName.ifBlank { original.authorName }
        val origAvatar = original.resharedFromAvatarUri.ifBlank { original.authorAvatarUri }
        val origText = original.resharedFromText.ifBlank { original.text }
        val origUid = original.resharedFromUid.ifBlank { original.userId }
        val origNoteId = original.resharedFromNoteId.ifBlank { original.id }
        val nowTs = Timestamp(Date(nowMillis))
        val expTs = Timestamp(Date(nowMillis + cleanHours * 3_600_000L))
        return StatusNote(
            id = newNoteId,
            userId = resharerUid,
            authorName = resharerName.ifBlank { "PGLOB Member" }.take(80),
            authorAvatarUri = resharerAvatarUri.take(500),
            text = clampNoteText(original.text),
            bgColor = presetForHex(original.bgColor).hex,
            music = original.music.take(80),
            mood = original.mood.take(40),
            privacy = "EVERYONE",
            isPublic = true,
            customAllowedUids = emptyList(),
            durationHours = cleanHours,
            likes = emptyList(),
            reshares = emptyList(),
            viewers = emptyList(),
            resharedFromNoteId = origNoteId,
            resharedFromUid = origUid,
            resharedFromName = origAuthor.take(80),
            resharedFromAvatarUri = origAvatar.take(500),
            resharedFromText = origText.take(120),
            edited = false,
            createdAt = nowTs,
            updatedAt = nowTs,
            expiresAt = expTs
        )
    }

    fun defaultFriendStatusNotes(nowMillis: Long = System.currentTimeMillis()): List<StatusNote> {
        val twoHoursAgo = nowMillis - 2 * 3_600_000L
        val thirtyMinsAgo = nowMillis - 30 * 60_000L
        val oneHourAgo = nowMillis - 60 * 60_000L
        val fourHoursAgo = nowMillis - 4 * 3_600_000L
        return listOf(
            StatusNote(
                id = "seed_note_amina",
                userId = "amina_ng",
                authorName = "Amina Yusuf",
                authorAvatarUri = "",
                text = "I dey Lagos today, DM for Ajo 🔥",
                bgColor = "#00A884",
                music = "Afrobeats Mix 🎵",
                mood = "🔥 Hot",
                privacy = "EVERYONE",
                isPublic = true,
                durationHours = 24,
                createdAt = Timestamp(Date(thirtyMinsAgo)),
                updatedAt = Timestamp(Date(thirtyMinsAgo)),
                expiresAt = Timestamp(Date(thirtyMinsAgo + 24 * 3_600_000L))
            ),
            StatusNote(
                id = "seed_note_zara",
                userId = "user_zara123",
                authorName = "Zara Bella",
                authorAvatarUri = "",
                text = "Golden hour shoot live soon ✨👑",
                bgColor = "#EC4899",
                music = "",
                mood = "😊 Happy",
                privacy = "EVERYONE",
                isPublic = true,
                durationHours = 24,
                createdAt = Timestamp(Date(oneHourAgo)),
                updatedAt = Timestamp(Date(oneHourAgo)),
                expiresAt = Timestamp(Date(oneHourAgo + 24 * 3_600_000L))
            ),
            StatusNote(
                id = "seed_note_marcus",
                userId = "marcus_us",
                authorName = "Marcus Vance",
                authorAvatarUri = "",
                text = "Live Music Room tonight 9PM GMT 🎶",
                bgColor = "#1E88E5",
                music = "Amapiano Vibes 🎶",
                mood = "🔥 Hot",
                privacy = "EVERYONE",
                isPublic = true,
                durationHours = 24,
                createdAt = Timestamp(Date(twoHoursAgo)),
                updatedAt = Timestamp(Date(twoHoursAgo)),
                expiresAt = Timestamp(Date(twoHoursAgo + 24 * 3_600_000L))
            ),
            StatusNote(
                id = "seed_note_blessing",
                userId = "blessing_ng",
                authorName = "Blessing Gold",
                authorAvatarUri = "",
                text = "Benin Ladies Ajo Escrow locked ✅",
                bgColor = "#FFD700",
                music = "",
                mood = "😊 Happy",
                privacy = "EVERYONE",
                isPublic = true,
                durationHours = 24,
                createdAt = Timestamp(Date(fourHoursAgo)),
                updatedAt = Timestamp(Date(fourHoursAgo)),
                expiresAt = Timestamp(Date(fourHoursAgo + 24 * 3_600_000L))
            )
        )
    }

    fun encodeNotesJson(notes: List<StatusNote>): String {
        val arr = JSONArray()
        notes.forEach { n ->
            arr.put(JSONObject().apply {
                put("id", n.id)
                put("userId", n.userId)
                put("authorName", n.authorName)
                put("authorAvatarUri", n.authorAvatarUri)
                put("text", n.text)
                put("bgColor", n.bgColor)
                put("music", n.music)
                put("mood", n.mood)
                put("privacy", n.privacy)
                put("isPublic", n.isPublic)
                put("customAllowedUids", JSONArray(n.customAllowedUids))
                put("durationHours", n.durationHours)
                put("likes", encodeActors(n.likes))
                put("reshares", encodeActors(n.reshares))
                put("viewers", encodeActors(n.viewers))
                put("resharedFromNoteId", n.resharedFromNoteId)
                put("resharedFromUid", n.resharedFromUid)
                put("resharedFromName", n.resharedFromName)
                put("resharedFromAvatarUri", n.resharedFromAvatarUri)
                put("resharedFromText", n.resharedFromText)
                put("edited", n.edited)
                put("createdAtMillis", n.createdAtMillisEffective())
                put("updatedAtMillis", n.updatedAtMillisEffective())
                put("expiresAtMillis", n.expiresAtMillisEffective())
            })
        }
        return arr.toString()
    }

    fun parseNotesJson(json: String): List<StatusNote> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val createdMs = o.optLong("createdAtMillis", 0L)
                    val updatedMs = o.optLong("updatedAtMillis", createdMs)
                    val expiresMs = o.optLong("expiresAtMillis", createdMs + 24 * 3_600_000L)
                    val customArr = o.optJSONArray("customAllowedUids") ?: JSONArray()
                    val customList = buildList {
                        for (j in 0 until customArr.length()) {
                            val s = customArr.optString(j)
                            if (s.isNotBlank()) add(s)
                        }
                    }
                    add(
                        StatusNote(
                            id = o.optString("id"),
                            userId = o.optString("userId"),
                            authorName = o.optString("authorName"),
                            authorAvatarUri = o.optString("authorAvatarUri"),
                            text = o.optString("text"),
                            bgColor = o.optString("bgColor", "#00A884"),
                            music = o.optString("music"),
                            mood = o.optString("mood"),
                            privacy = o.optString("privacy", "EVERYONE"),
                            isPublic = o.optBoolean("isPublic", true),
                            customAllowedUids = customList,
                            durationHours = o.optInt("durationHours", 24),
                            likes = parseActors(o.optJSONArray("likes")),
                            reshares = parseActors(o.optJSONArray("reshares")),
                            viewers = parseActors(o.optJSONArray("viewers")),
                            resharedFromNoteId = o.optString("resharedFromNoteId"),
                            resharedFromUid = o.optString("resharedFromUid"),
                            resharedFromName = o.optString("resharedFromName"),
                            resharedFromAvatarUri = o.optString("resharedFromAvatarUri"),
                            resharedFromText = o.optString("resharedFromText"),
                            edited = o.optBoolean("edited", false),
                            createdAt = if (createdMs > 0L) Timestamp(Date(createdMs)) else null,
                            updatedAt = if (updatedMs > 0L) Timestamp(Date(updatedMs)) else null,
                            expiresAt = if (expiresMs > 0L) Timestamp(Date(expiresMs)) else null
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeActors(actors: List<StatusNoteActor>): JSONArray = JSONArray().apply {
        actors.forEach { a ->
            put(JSONObject().apply {
                put("uid", a.uid)
                put("name", a.name)
                put("photoUri", a.photoUri)
                put("timestampMillis", a.timestampMillis)
            })
        }
    }

    private fun parseActors(arr: JSONArray?): List<StatusNoteActor> {
        if (arr == null) return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val uid = o.optString("uid")
                if (uid.isNotBlank()) {
                    add(
                        StatusNoteActor(
                            uid = uid,
                            name = o.optString("name"),
                            photoUri = o.optString("photoUri"),
                            timestampMillis = o.optLong("timestampMillis", 0L)
                        )
                    )
                }
            }
        }
    }
}
