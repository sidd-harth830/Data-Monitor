package com.siddharth.datamonitor.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.siddharth.datamonitor.ui.theme.ThemeManager

@Composable
fun AuthGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                clip = true
                this.shape = shape
            }
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                shape = shape
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            )
    ) {
        // Frosty blur background
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
        )
        
        Box(
            modifier = Modifier,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    themeManager: ThemeManager,
    onLoginSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val accentColor = Color(0xFF19B1DC) // Modern neon blue cyan
    val neonPinkColor = Color(0xFFFF2A85) // Neon magenta accent

    // Dynamic resource lookup for default_web_client_id 
    val defaultWebClientId = remember {
        val resourceId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resourceId != 0) context.getString(resourceId) else "236997686171-pe3q3sktef5o2cug9vo0dtc22celp14u.apps.googleusercontent.com"
    }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(defaultWebClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                isLoading = true
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        isLoading = false
                        val user = auth.currentUser
                        if (user != null) {
                            com.siddharth.datamonitor.utils.UserTelemetrySync.sync(user.uid, user.email, "Google")
                        }
                        Toast.makeText(context, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.localizedMessage ?: "Google Authenticator Sync Failed"
                    }
            } else {
                isLoading = false
                errorMessage = "Failed to extract Google Identification Token"
            }
        } catch (e: ApiException) {
            isLoading = false
            errorMessage = "Google Sign-In Error (Code: ${e.statusCode})"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070E18)) // Pure premium dark background (#070E18)
    ) {
        // High contrast glowing layered auras in the background
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(280.dp)
                .offset(x = 60.dp, y = (-60).dp)
                .background(Brush.radialGradient(listOf(accentColor.copy(alpha = 0.22f), Color.Transparent)))
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(340.dp)
                .offset(x = (-90).dp, y = 100.dp)
                .background(Brush.radialGradient(listOf(neonPinkColor.copy(alpha = 0.16f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "DATA MONITOR",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "V2.3.0 CORE ENGINE ATTAINED",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            AuthGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Authentication Portal",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF5252),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = accentColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = accentColor) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                Icon(icon, contentDescription = "Password Visibility", tint = Color.White.copy(alpha = 0.6f))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(108.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = accentColor)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter email and password"
                                    return@Button
                                  }
                                isLoading = true
                                auth.signInWithEmailAndPassword(email.trim(), password.trim())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        val user = auth.currentUser
                                        if (user != null) {
                                            com.siddharth.datamonitor.utils.UserTelemetrySync.sync(user.uid, user.email, "Email")
                                        }
                                        Toast.makeText(context, "Welcome Overlord", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Sign In Failed"
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("LOGIN", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                if (email.isBlank() || password.isBlank() || password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                    return@OutlinedButton
                                }
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                                    .addOnSuccessListener {
                                        isLoading = false
                                        val user = auth.currentUser
                                        if (user != null) {
                                            com.siddharth.datamonitor.utils.UserTelemetrySync.sync(user.uid, user.email, "Email")
                                        }
                                        Toast.makeText(context, "Account Register Succeeded", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Account Creation Failed"
                                    }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.4f)))
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.12f))
                            Text(
                                text = "OR CONTINUE WITH",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.12f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Google Sign-In Card
                            OutlinedButton(
                                onClick = {
                                    isLoading = true
                                    val signInIntent = googleSignInClient.signInIntent
                                    googleSignInLauncher.launch(signInIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)))
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Canvas(modifier = Modifier.size(8.dp)) {
                                        drawCircle(color = Color(0xFFEA4335))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Google", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // GitHub Sign-In Card
                            OutlinedButton(
                                onClick = {
                                    if (activity != null) {
                                        isLoading = true
                                        val provider = OAuthProvider.newBuilder("github.com")
                                        auth.startActivityForSignInWithProvider(activity, provider.build())
                                            .addOnSuccessListener {
                                                isLoading = false
                                                val user = auth.currentUser
                                                if (user != null) {
                                                    com.siddharth.datamonitor.utils.UserTelemetrySync.sync(user.uid, user.email, "GitHub")
                                                }
                                                Toast.makeText(context, "Welcome Overlord from GitHub!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = e.localizedMessage ?: "GitHub Federated Sync Failed"
                                            }
                                    } else {
                                        errorMessage = "Internal Error: Active Activity missing."
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f)))
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Canvas(modifier = Modifier.size(8.dp)) {
                                        drawCircle(color = Color(0xFFE2E4E6))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("GitHub", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = {
                    val androidID = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: java.util.UUID.randomUUID().toString()
                    com.siddharth.datamonitor.utils.UserTelemetrySync.sync("guest_$androidID", "Guest User", "Guest")
                    onSkip()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "SKIP / CONTINUE AS GUEST →",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    fontSize = 13.sp
                )
            }
        }
    }
}
