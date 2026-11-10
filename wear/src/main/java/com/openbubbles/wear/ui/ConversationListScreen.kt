package com.openbubbles.wear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TimeText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import com.openbubbles.wear.WatchRepository
import com.openbubbles.wear.WatchState
import com.openbubbles.wear.protocol.ThreadSummary

@Composable
fun ConversationListScreen(
    state: WatchState,
    onOpenThread: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onFindChat: () -> Unit,
    onSync: () -> Unit,
    onOpenPhone: () -> Unit,
    onDismissNotice: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val conversations = remember(state.conversations) { state.conversationsForDisplay }
    val unreadCount = conversations.count { it.unread }

    AppScaffold(timeText = { TimeText() }) {
        Box(Modifier.fillMaxSize()) {
            if (state.loading) {
                FullScreenLoading("Looking for your phone…")
                return@Box
            }
            var pullDistance by remember { mutableStateOf(0f) }
            var pullTriggered by remember { mutableStateOf(false) }
            val pullThresholdPx = with(LocalDensity.current) { 56.dp.toPx() }
            LaunchedEffect(state.syncing) {
                if (!state.syncing) pullTriggered = false
            }
            val pullToRefreshConnection = object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    if (source == NestedScrollSource.UserInput &&
                        listState.firstVisibleItemIndex == 0 &&
                        listState.firstVisibleItemScrollOffset == 0 &&
                        available.y > 0f
                    ) {
                        pullDistance += available.y
                        if (pullDistance > pullThresholdPx && !pullTriggered) {
                            pullTriggered = true
                            onSync()
                        }
                    }
                    return Offset.Zero
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    pullDistance = 0f
                    return Velocity.Zero
                }
            }
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshConnection),
                autoCentering = null,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 30.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (pullTriggered) {
                    item(key = "refreshing") {
                        Text(
                            text = if (state.syncing) "Refreshing…" else "Syncing…",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                val notice = state.error
                if (notice != null) {
                    item {
                        NoticeCard(
                            message = notice,
                            isError = state.error != null,
                            onDismiss = onDismissNotice
                        )
                    }
                }

                if (conversations.isEmpty()) {
                    item {
                        EmptyStateCard(
                            directBridge = state.directBridge,
                            phoneOnline = state.phoneOnline,
                            onSync = onSync
                        )
                    }
                } else {
                    items(
                        items = conversations,
                        key = { it.id }
                    ) { summary ->
                        ConversationCard(
                            state = state,
                            summary = summary,
                            onClick = { onOpenThread(summary.id) }
                        )
                    }
                }

                if (state.error == null && state.info != null) {
                    item(key = "info_notice") {
                        NoticeCard(message = state.info, isError = false, onDismiss = onDismissNotice)
                    }
                }

                item {
                    TextButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Settings")
                    }
                }

                if (!state.phoneOnline) {
                    item {
                        TextButton(
                            onClick = onOpenPhone,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open on phone")
                        }
                    }
                }

                item {
                    SectionHeader(text = WATCH_APP_VERSION)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    directBridge: Boolean,
    phoneOnline: Boolean,
    onSync: () -> Unit
) {
    Card(
        onClick = onSync,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "No conversations yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = when {
                    !phoneOnline ->
                        "Open the bridge app on your phone, then sync again."

                    directBridge ->
                        "Choose a contact to start a conversation, or tap to sync."

                    else ->
                        "Enable the Wear patch in OpenBubbles to load conversations."
                },
                style = MaterialTheme.typography.bodyExtraSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap to sync",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ConversationCard(
    state: WatchState,
    summary: ThreadSummary,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (summary.unread) scheme.surfaceContainerHigh else scheme.surfaceContainerLow
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ContactAvatar(
                state = state,
                avatarId = summary.avatarId,
                initials = initialsFor(summary.title),
                color = if (summary.unread) scheme.primaryContainer else scheme.surfaceContainerHigh,
                contentColor = if (summary.unread) scheme.onPrimaryContainer else scheme.onSurface
            )
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = summary.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (summary.unread) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (summary.unread) {
                        Spacer(Modifier.width(5.dp))
                        StatusDot(scheme.primary)
                    }
                }
                Text(
                    text = summary.lastPreview.ifBlank { "No messages" },
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (summary.isGroup) "Group" else "Chat",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = relativeTime(summary.lastTimestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
