package com.oudine.redapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oudine.redapp.data.SupabaseProvider
import io.github.jan.supabase.auth.providers.Email
import kotlinx.coroutines.launch

private val Ink = Color(0xFF07111C)
private val Panel = Color(0xFF101F2D)
private val Green = Color(0xFF24D18B)
private val Blue = Color(0xFF2388F4)
private val Soft = Color(0xFFB7C7D8)

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
            primary = Green,
            secondary = Blue,
            background = Ink,
            surface = Panel,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Composable
private fun RedAppRoot() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var signedIn by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Ink, Color(0xFF0A1D2C), Color(0xFF07111C)))
        )
    ) {
        if (signedIn) {
            HomeScreen(onSignOut = {
                scope.launch {
                    runCatching { SupabaseProvider.client.auth.signOut() }
                    signedIn = false
                }
            })
        } else {
            Column(
                Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("œ", fontSize = 68.sp, fontWeight = FontWeight.Black, color = Green)
                Text("RedApp", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("from Oudine", color = Soft, fontSize = 14.sp)
                Spacer(Modifier.height(34.dp))
                Text(if (isSignUp) "Create your account" else "Welcome back",
                    fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (!SupabaseProvider.isConfigured) {
                            message = "Add your Supabase publishable key in local.properties first."
                            return@Button
                        }
                        if (email.isBlank() || password.length < 6) {
                            message = "Enter a valid email and a password of at least 6 characters."
                            return@Button
                        }
                        scope.launch {
                            busy = true
                            message = ""
                            val result = runCatching {
                                if (isSignUp) {
                                    SupabaseProvider.client.auth.signUpWith(Email) {
                                        this.email = email.trim()
                                        this.password = password
                                    }
                                } else {
                                    SupabaseProvider.client.auth.signInWith(Email) {
                                        this.email = email.trim()
                                        this.password = password
                                    }
                                }
                            }
                            busy = false
                            result.onSuccess {
                                if (isSignUp) message = "Account request sent. Check your email if confirmation is enabled."
                                else signedIn = true
                            }.onFailure {
                                message = it.message ?: "Authentication failed. Please try again."
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Ink)
                ) {
                    if (busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(if (isSignUp) "Create account" else "Sign in", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { isSignUp = !isSignUp; message = "" }) {
                    Text(if (isSignUp) "Already have an account? Sign in" else "New to RedApp? Create account")
                }
                if (message.isNotBlank()) {
                    Text(message, color = if (message.startsWith("Account")) Green else Soft,
                        modifier = Modifier.padding(top = 8.dp))
                }
                if (!SupabaseProvider.isConfigured) {
                    Text("Supabase setup required", color = Blue, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("RedApp", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Stay close. Stay connected.", color = Soft)
            }
            Text("œ", fontSize = 34.sp, color = Green, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(32.dp))
        FeatureTile(Icons.Rounded.ChatBubble, "Messages", "Your private conversations")
        FeatureTile(Icons.Rounded.Groups, "Groups", "Bring friends together")
        FeatureTile(Icons.Rounded.PersonAdd, "Connect", "Phone number • QR code • RedApp ID")
        Spacer(Modifier.weight(1f))
        Text("More RedApp features are being connected next.", color = Soft)
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out")
        }
    }
}

@Composable
private fun FeatureTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Green, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(subtitle, color = Soft, fontSize = 13.sp)
            }
        }
    }
}
