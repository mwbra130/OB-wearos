package com.openbubbles.wear

import com.openbubbles.wear.protocol.MessageReaction
import com.openbubbles.wear.protocol.MessageReference
import com.openbubbles.wear.protocol.SyncPayload
import com.openbubbles.wear.protocol.WireMessage
import com.openbubbles.wear.protocol.WireThread

/**
 * TEST ONLY — removable.
 *
 * Seeds fake conversations into the repository so the watch UI can be checked
 * without the phone bridge. Loaded and cleared from Settings > Test mode.
 * The fake chats disappear for good after a real phone sync replaces them.
 * Delete this file and the Settings Test mode card to remove the feature.
 */
object FakeChats {

    fun load() {
        val now = System.currentTimeMillis()

        val maya = WireThread(
            id = "fake-maya",
            title = "Maya Chen",
            participants = listOf("Maya Chen"),
            unread = true,
            canReply = true,
            canMarkRead = true,
            updatedAt = now,
            messages = listOf(
                msg("fake-maya-1", "fake-maya", "Are we still on for dinner tonight?", now - 52 * 60_000L, "Maya Chen"),
                msg("fake-maya-2", "fake-maya", "Yes! 7pm at the usual place?", now - 49 * 60_000L, fromMe = true, status = "read"),
                msg("fake-maya-3", "fake-maya", "Perfect, I will book a table for four. Also reminding myself this is a longer message so we can see how a wrapped bubble looks on the round screen.", now - 44 * 60_000L, "Maya Chen",
                    reactions = listOf(MessageReaction(type = "love", count = 1, fromMe = true))),
                msg("fake-maya-4", "fake-maya", "Can you bring the book you borrowed?", now - 12 * 60_000L, "Maya Chen"),
                msg("fake-maya-5", "fake-maya", "Got it, packing it now", now - 10 * 60_000L, fromMe = true, status = "delivered"),
                msg("fake-maya-6", "fake-maya", "See you soon!", now - 3 * 60_000L, "Maya Chen")
            )
        )

        val family = WireThread(
            id = "fake-family",
            title = "Family",
            isGroup = true,
            participants = listOf("Mom", "Dad", "Sam", "Me"),
            unread = true,
            canReply = true,
            canMarkRead = true,
            updatedAt = now,
            messages = listOf(
                msg("fake-family-1", "fake-family", "Sunday lunch at ours?", now - 3 * 3_600_000L, "Mom"),
                msg("fake-family-2", "fake-family", "Works for me", now - 2 * 3_600_000L, "Dad"),
                msg("fake-family-3", "fake-family", "I can drive", now - 100 * 60_000L, "Sam"),
                msg("fake-family-4", "fake-family", "I will bring dessert", now - 90 * 60_000L, fromMe = true, status = "read",
                    replyTo = MessageReference(messageId = "fake-family-1", text = "Sunday lunch at ours?", sender = "Mom")),
                msg("fake-family-5", "fake-family", "Amazing, see you Sunday", now - 62 * 60_000L, "Mom")
            )
        )

        val longName = WireThread(
            id = "fake-longname",
            title = "Alexandria Featherstonehaugh",
            participants = listOf("Alexandria Featherstonehaugh"),
            canReply = true,
            canMarkRead = true,
            updatedAt = now,
            messages = listOf(
                msg("fake-long-1", "fake-longname", "This chat tests a very long name in the header", now - 26 * 3_600_000L, "Alexandria Featherstonehaugh"),
                msg("fake-long-2", "fake-longname", "Header should stay compact", now - 25 * 3_600_000L, fromMe = true, status = "sent")
            )
        )

        val blank = WireThread(
            id = "fake-blank",
            title = "",
            participants = emptyList(),
            canReply = true,
            canMarkRead = false,
            updatedAt = now,
            messages = listOf(
                msg("fake-blank-1", "fake-blank", "This chat has no title, like the deleted one", now - 21 * 60_000L, null),
                msg("fake-blank-2", "fake-blank", "On my way", now - 20 * 60_000L, null)
            )
        )

        val bus = WireThread(
            id = "fake-bus",
            title = "School Bus Updates",
            participants = listOf("School Bus Updates"),
            canReply = false,
            canMarkRead = false,
            updatedAt = now,
            messages = listOf(
                msg("fake-bus-1", "fake-bus", "Route 12 is running 10 minutes late", now - 5 * 3_600_000L, "School Bus Updates", locked = true),
                msg("fake-bus-2", "fake-bus", "Read only chat: no reply buttons should work here", now - 4 * 3_600_000L, "School Bus Updates", locked = true)
            )
        )

        val coachMessages = (1..12).map { i ->
            msg(
                id = "fake-coach-$i",
                threadId = "fake-coach",
                text = if (i % 2 == 0) "Practice note $i: keep your cadence up on the hills" else "Got it, see you at $i",
                timestamp = now - (13 - i) * 7 * 60_000L,
                sender = if (i % 2 == 0) "Coach" else null,
                fromMe = i % 2 != 0,
                status = if (i % 2 != 0) "delivered" else null
            )
        }
        val coach = WireThread(
            id = "fake-coach",
            title = "Coach",
            participants = listOf("Coach"),
            canReply = true,
            canMarkRead = true,
            updatedAt = now,
            messages = coachMessages
        )

        val threads = listOf(maya, family, longName, blank, bus, coach)
        val payload = SyncPayload(
            generatedAt = now,
            phoneName = "Fake phone (test)",
            bridgeVersion = "test",
            relayEnabled = true,
            conversations = threads.map { it.toSummary() }
        )
        WatchRepository.onSync(payload)
        threads.forEach { WatchRepository.onThread(it) }
    }

    /** Removes the fake chats; the caller should sync right after to pull the real list back. */
    fun clear() {
        val now = System.currentTimeMillis()
        val payload = SyncPayload(
            generatedAt = now,
            phoneName = "",
            bridgeVersion = "",
            relayEnabled = true,
            conversations = emptyList()
        )
        WatchRepository.onSync(payload)
    }

    private fun msg(
        id: String,
        threadId: String,
        text: String,
        timestamp: Long,
        sender: String? = null,
        fromMe: Boolean = false,
        status: String? = null,
        reactions: List<MessageReaction> = emptyList(),
        replyTo: MessageReference? = null,
        locked: Boolean = false
    ) = WireMessage(
        id = id,
        threadId = threadId,
        text = text,
        timestamp = timestamp,
        sender = sender,
        fromMe = fromMe,
        status = status,
        reactions = reactions,
        canReact = !locked,
        canReply = !locked,
        replyTo = replyTo
    )
}
