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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oudine.redapp.data.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
        else -> EmailAuthScreen(onAuthenticated = { authenticated = true })
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
                    AppTab.Calls to Icons.Rounded.Call,
                    AppTab.Connect to Icons.Rounded.PersonAdd,
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
                AppTab.Calls -> CallsPage()
                AppTab.Connect -> ConnectPage()
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


@Composable
private fun EmailAuthScreen(
    onAuthenticated: () -> Unit,
    configurationError: String? = null
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf(configurationError.orEmpty()) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NightBlue, Night))),
        contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("RedApp", color = White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("Secure account verification", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CardBlue), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(if (sent) "Check your email" else "Sign in or create account",
                        color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(if (sent) "Enter the one-time code sent to $email."
                        else "RedApp sends a one-time verification code to your email. No phone number required.",
                        color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it.trim() },
                        modifier = Modifier.fillMaxWidth(), enabled = !sent && !busy,
                        label = { Text("Email address") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                    if (sent) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = token, onValueChange = { token = it.filter(Char::isDigit).take(8) },
                            modifier = Modifier.fillMaxWidth(), enabled = !busy,
                            label = { Text("Verification code") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    if (message.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(message, color = if (message.startsWith("Error")) Color(0xFFFF8A8A) else Cyan, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                busy = true
                                message = ""
                                try {
                                    if (!sent) {
                                        require(email.contains("@") && email.contains(".")) { "Enter a valid email address." }
                                        SupabaseProvider.client.auth.signInWith(OTP) { this.email = email }
                                        sent = true
                                        message = "Verification code sent. Check your inbox and spam folder."
                                    } else {
                                        require(token.length >= 6) { "Enter the code from your email." }
                                        SupabaseProvider.client.auth.verifyEmailOtp(
                                            type = OtpType.Email.EMAIL, email = email, token = token
                                        )
                                        onAuthenticated()
                                    }
                                } catch (e: Exception) {
                                    message = "Error: ${e.message ?: "Please try again."}"
                                } finally { busy = false }
                            }
                        },
                        enabled = !busy && configurationError == null &&
                            (if (sent) token.length >= 6 else email.isNotBlank()),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red)
                    ) {
                        if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = White)
                        else Text(if (sent) "Verify and continue" else "Send email code")
                    }
                    if (sent) TextButton(onClick = { sent = false; token = ""; message = "" }) {
                        Text("Change email", color = Cyan)
                    }
                }
            }
        }
    }
}

@Serializable
private data class RedAppCallInvite(
    @SerialName("caller_id") val callerId: String,
    @SerialName("callee_id") val calleeId: String,
    @SerialName("call_type") val callType: String,
    val status: String = "ringing"
)

@Composable
private fun CallsPage() {
    val scope = rememberCoroutineScope()
    var recipient by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("audio") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Start an internet call using your friend's RedApp user ID.") }

    Text("Calls", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    Text("Voice and video call invitations", color = Muted, fontSize = 13.sp,
        modifier = Modifier.padding(top = 5.dp, bottom = 16.dp))
    Card(colors = CardDefaults.cardColors(containerColor = CardBlue), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            OutlinedTextField(value = recipient, onValueChange = { recipient = it.trim() },
                modifier = Modifier.fillMaxWidth(), label = { Text("Friend's RedApp user ID") }, singleLine = true)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = type == "audio", onClick = { type = "audio" },
                    label = { Text("Voice") }, leadingIcon = { Icon(Icons.Rounded.Call, null) })
                FilterChip(selected = type == "video", onClick = { type = "video" },
                    label = { Text("Video") }, leadingIcon = { Icon(Icons.Rounded.Videocam, null) })
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        try {
                            val caller = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id
                                ?: error("Sign in again to make a call.")
                            require(recipient.matches(Regex("^[0-9a-fA-F-]{36}$")) && recipient != caller) {
                                "Enter a valid friend's RedApp user ID."
                            }
                            SupabaseProvider.client.postgrest["calls"].insert(
                                RedAppCallInvite(callerId = caller, calleeId = recipient, callType = type)
                            )
                            status = "Call invitation saved on the RedApp server. Live audio/video media is not connected yet."
                        } catch (e: Exception) {
                            status = "Error: ${e.message ?: "Could not create call."}"
                        } finally { busy = false }
                    }
                },
                enabled = !busy && recipient.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) {
                if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = White)
                else Icon(Icons.Rounded.Call, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create call invitation")
            }
            Spacer(Modifier.height(10.dp))
            Text(status, color = if (status.startsWith("Error")) Color(0xFFFF8A8A) else Muted, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("Calls use Wi-Fi or mobile data, not paid phone minutes.", color = Cyan, fontSize = 12.sp)
}
