package com.oudine.redapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oudine.redapp.data.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

private val Night = Color(0xFF060B1E)
private val NightBlue = Color(0xFF0A1230)
private val CardBlue = Color(0xFF101B3B)
private val Red = Color(0xFFFF2D3F)
private val Cyan = Color(0xFF00D4FF)
private val Blue = Color(0xFF1E7BFF)
private val Muted = Color(0xFF9AA7CC)
private val White = Color(0xFFF8FAFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RedAppTheme { RedAppRoot() } }
    }
}

@Composable
private fun RedAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Red,
            secondary = Cyan,
            background = Night,
            surface = CardBlue,
            onBackground = White,
            onSurface = White
        ),
        content = content
    )
}

@Composable
private fun RedAppRoot() {
    var authenticated by remember { mutableStateOf(false) }
    var checkingSession by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        authenticated = try {
            SupabaseProvider.client.auth.currentSessionOrNull() != null
        } catch (_: Throwable) {
            false
        }
        checkingSession = false
    }

    when {
        checkingSession -> Box(
            Modifier.fillMaxSize().background(Night),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = Cyan) }
        authenticated -> MainShell(onSignOut = { authenticated = false })
        else -> EmailVerificationScreen(onVerified = { authenticated = true })
    }
}

private enum class AppTab(val label: String) {
    Chats("Chats"), Groups("Groups"), Connect("Connect"), Calls("Calls"), Profile("Profile")
}

@Composable
private fun MainShell(onSignOut: () -> Unit) {
    var selected by remember { mutableStateOf(AppTab.Chats) }
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF090F25), contentColor = White) {
                val tabs = listOf(
                    AppTab.Chats to Icons.Rounded.ChatBubble,
                    AppTab.Groups to Icons.Rounded.Groups,
                    AppTab.Connect to Icons.Rounded.PersonAdd,
                    AppTab.Calls to Icons.Rounded.Call,
                    AppTab.Profile to Icons.Rounded.Person
                )
                tabs.forEach { (tab, icon) ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = { Icon(icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan,
                            selectedTextColor = Cyan,
                            indicatorColor = Color(0xFF14264B),
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(listOf(NightBlue, Night)))
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("RedApp", color = White, fontSize = 29.sp, fontWeight = FontWeight.Black)
                    Text("Private chat that starts with a scan", color = Muted, fontSize = 12.sp)
                }
                Box(
                    Modifier.size(44.dp).background(
                        Brush.linearGradient(listOf(Blue, Cyan)), CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) { Text("œ", color = White, fontSize = 28.sp, fontWeight = FontWeight.Black) }
            }
            when (selected) {
                AppTab.Chats -> ChatsPage()
                AppTab.Groups -> SimplePage("Your groups", "Bring your people together in private groups.", Icons.Rounded.Groups)
                AppTab.Connect -> ConnectPage()
                AppTab.Calls -> CallsPage()
                AppTab.Profile -> ProfilePage(onSignOut)
            }
        }
    }
}

@Composable
private fun ChatsPage() {
    var query by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        Text("Messages", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        IconButton(onClick = {}) { Icon(Icons.Rounded.Edit, contentDescription = "New chat", tint = Cyan) }
    }
    OutlinedTextField(
        value = query, onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
        placeholder = { Text("Search conversations", color = Muted) },
        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Muted) },
        singleLine = true, shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Cyan, unfocusedBorderColor = Color(0xFF29375B),
            focusedTextColor = White, unfocusedTextColor = White
        )
    )
    Text("RECENT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp,
        modifier = Modifier.padding(bottom = 10.dp))
    val demo = listOf(
        Triple("Your RedApp space", "Start by connecting with a friend", "Now"),
        Triple("Private conversations", "Your chats will appear here", ""),
        Triple("RedApp groups", "Create a group with your people", "")
    ).filter { it.first.contains(query, true) || it.second.contains(query, true) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(demo) { item ->
            Row(
                Modifier.fillMaxWidth().background(CardBlue, RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF25365C), RoundedCornerShape(18.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(48.dp).background(
                    Brush.linearGradient(listOf(Blue, Cyan)), CircleShape
                ), contentAlignment = Alignment.Center) {
                    Text(item.first.take(1), color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.first, color = White, fontWeight = FontWeight.SemiBold)
                    Text(item.second, color = Muted, fontSize = 12.sp)
                }
                Text(item.third, color = Muted, fontSize = 11.sp)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111F42)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lock, null, tint = Cyan)
            Spacer(Modifier.width(10.dp))
            Text("Your private space. Connect with someone to start chatting.", color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SimplePage(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Text(title, color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(14.dp))
    Card(colors = CardDefaults.cardColors(containerColor = CardBlue), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Cyan, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(12.dp))
            Text(subtitle, color = Muted)
        }
    }
}

@Composable
private fun ConnectPage() {
    Text("Connect with friends", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    Text("Choose how you want to find someone.", color = Muted, fontSize = 13.sp,
        modifier = Modifier.padding(top = 5.dp, bottom = 18.dp))
    listOf(
        Triple(Icons.Rounded.Phone, "Phone number", "Find someone using their number"),
        Triple(Icons.Rounded.QrCode2, "Scan QR code", "Connect instantly by scanning"),
        Triple(Icons.Rounded.Tag, "RedApp ID", "Enter their unique RedApp ID")
    ).forEach { item ->
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { },
            colors = CardDefaults.cardColors(containerColor = CardBlue),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(item.first, null, tint = Cyan, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.second, color = White, fontWeight = FontWeight.SemiBold)
                    Text(item.third, color = Muted, fontSize = 12.sp)
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = Muted)
            }
        }
    }
}

@Composable
private fun CallsPage() {
    val scope = rememberCoroutineScope()
    var recipientId by remember { mutableStateOf("") }
    var callType by remember { mutableStateOf("audio") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Enter your friend's Supabase user ID to create a call invitation.") }
    var isError by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Calls", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Internet voice and video call invitations", color = Muted, fontSize = 13.sp)
        Card(colors = CardDefaults.cardColors(containerColor = CardBlue), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = recipientId,
                    onValueChange = { recipientId = it.trim() },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Friend's account UUID") },
                    placeholder = { Text("Paste the recipient user ID") },
                    shape = RoundedCornerShape(14.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = callType == "audio", onClick = { callType = "audio" }, label = { Text("Voice") })
                    FilterChip(selected = callType == "video", onClick = { callType = "video" }, label = { Text("Video") })
                }
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            isError = false
                            try {
                                val uuid = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$")
                                require(uuid.matches(recipientId)) { "Enter a valid account UUID." }
                                SupabaseProvider.client.functions.invoke(
                                    function = "call-invite",
                                    body = kotlinx.serialization.json.buildJsonObject {
                                        put("callee_id", recipientId)
                                        put("call_type", callType)
                                    }
                                )
                                message = "Call invitation created. The live audio/video connection is the next integration step."
                            } catch (error: Throwable) {
                                isError = true
                                message = error.message ?: "Could not create the call invitation."
                            } finally {
                                busy = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !busy
                ) {
                    if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
                    else {
                        Icon(if (callType == "audio") Icons.Rounded.Call else Icons.Rounded.Videocam, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (callType == "audio") "Start voice call" else "Start video call")
                    }
                }
                Text(message, color = if (isError) Color(0xFFFF7A88) else Muted, fontSize = 12.sp)
            }
        }
        Text("Calls use the internet. Your mobile operator's voice minutes are not used.", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun ProfilePage(onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    Text("Your profile", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(18.dp))
    Card(colors = CardDefaults.cardColors(containerColor = CardBlue), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).background(Brush.linearGradient(listOf(Blue, Cyan)), CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Person, null, tint = White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("RedApp member", color = White, fontWeight = FontWeight.Bold)
                Text("Verified email account", color = Muted, fontSize = 12.sp)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    OutlinedButton(
        onClick = {
            scope.launch {
                try { SupabaseProvider.client.auth.signOut() } finally { onSignOut() }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Sign out") }
}
