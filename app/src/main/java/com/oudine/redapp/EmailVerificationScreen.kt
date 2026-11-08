package com.oudine.redapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
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
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.launch

@Composable
fun EmailVerificationScreen(onVerified: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf(false) }
    val client = SupabaseProvider.client

    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1230), Color(0xFF060B1E))))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("RedApp", color = Color(0xFFF8FAFF), fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text("Secure sign-in with your email", color = Color(0xFF9AA7CC))
        Spacer(Modifier.height(28.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B3B)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(if (codeSent) "Check your inbox" else "Verify your email",
                    color = Color(0xFFF8FAFF), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (codeSent) "Enter the 6-digit code sent to $email."
                    else "We'll email you a one-time code. No phone number or SMS is needed.",
                    color = Color(0xFF9AA7CC), fontSize = 13.sp
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(), enabled = !codeSent && !busy,
                    singleLine = true, label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp)
                )
                if (codeSent) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                        modifier = Modifier.fillMaxWidth(), enabled = !busy,
                        singleLine = true, label = { Text("6-digit verification code") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                message?.let { Text(it, color = if (error) Color(0xFFFF7A88) else Color(0xFF75E5C2), fontSize = 12.sp) }
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            message = null
                            try {
                                if (!codeSent) {
                                    if (!email.contains("@") || email.length < 5) {
                                        message = "Enter a valid email address."
                                        error = true
                                    } else {
                                        client.auth.signInWith(OTP) { this.email = email.trim() }
                                        codeSent = true
                                        error = false
                                        message = "Code sent. Check your inbox and spam folder."
                                    }
                                } else if (code.length != 6) {
                                    message = "Enter the complete 6-digit code."
                                    error = true
                                } else {
                                    client.auth.verifyEmailOtp(
                                        type = OtpType.Email.EMAIL,
                                        email = email.trim(),
                                        token = code
                                    )
                                    onVerified()
                                }
                            } catch (e: Throwable) {
                                message = e.message ?: "Something went wrong. Please try again."
                                error = true
                            } finally {
                                busy = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D3F)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(if (codeSent) "Verify and continue" else "Send verification code", fontWeight = FontWeight.Bold)
                }
                if (codeSent) {
                    TextButton(onClick = { codeSent = false; code = ""; message = null }, enabled = !busy,
                        modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Change email address", color = Color(0xFF00D4FF))
                    }
                }
            }
        }
    }
}
