package com.siddharth.datamonitor.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.siddharth.datamonitor.BuildConfig
import com.siddharth.datamonitor.ui.theme.ThemeManager

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
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val accentColor = MaterialTheme.colorScheme.primary
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f

    // Dynamic resource lookup for default_web_client_id 
    val defaultWebClientId = remember {
        val resourceId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resourceId != 0) context.getString(resourceId) else "236997686171-i3j44ute7mnbde7tvc4o6mnnr5njlol5.apps.googleusercontent.com"
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
                        com.siddharth.datamonitor.utils.AnalyticsHelper.logCrash("GoogleSignIn", e)
                        if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                            errorMessage = "Account exists. Please use the method you originally signed up with."
                            Toast.makeText(context, "Account exists. Please use the method you originally signed up with.", Toast.LENGTH_LONG).show()
                        } else {
                            errorMessage = e.localizedMessage ?: "Google Authenticator Sync Failed"
                        }
                    }
            } else {
                isLoading = false
                errorMessage = "Failed to extract Google Identification Token"
                com.siddharth.datamonitor.utils.AnalyticsHelper.logCrash("GoogleSignIn_ExtractToken", Exception("Failed to extract Google Identification Token"))
            }
        } catch (e: Exception) {
            isLoading = false
            com.siddharth.datamonitor.utils.AnalyticsHelper.logCrash("GoogleSignIn_Catch", e)
            if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                errorMessage = "Account exists. Please use the method you originally signed up with."
                Toast.makeText(context, "Account exists. Please use the method you originally signed up with.", Toast.LENGTH_LONG).show()
            } else {
                errorMessage = "Google Sign-In Error: ${e.localizedMessage}"
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "V${BuildConfig.VERSION_NAME} SECURE CORE OVERLORD",
                color = if (isLight) Color.Black else Color.White,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            GlassCard(
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
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Required Name field for registration
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Full Name (Required for sign-up only)", color = MaterialTheme.colorScheme.onSurface) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name", tint = accentColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address", color = MaterialTheme.colorScheme.onSurface) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = accentColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password", color = MaterialTheme.colorScheme.onSurface) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = accentColor) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                Icon(icon, contentDescription = "Password Visibility", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface
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
                                        com.siddharth.datamonitor.utils.AnalyticsHelper.logCrash("EmailSignIn", e)
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                            shape = RoundedCornerShape(4.dp), // Stark flat styling
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("LOGIN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                if (name.isBlank()) {
                                    errorMessage = "Name is required for registration"
                                    return@OutlinedButton
                                }
                                if (email.isBlank() || password.isBlank() || password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                    return@OutlinedButton
                                }
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                                    .addOnSuccessListener { authResult ->
                                        val user = authResult.user
                                        if (user != null) {
                                            val profileUpdates = UserProfileChangeRequest.Builder()
                                                .setDisplayName(name.trim())
                                                .build()
                                            user.updateProfile(profileUpdates)
                                                .addOnCompleteListener { profileTask ->
                                                    isLoading = false
                                                    if (profileTask.isSuccessful) {
                                                        com.siddharth.datamonitor.utils.UserTelemetrySync.sync(user.uid, user.email, "Email")
                                                    }
                                                    Toast.makeText(context, "Account Register Succeeded", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                }
                                        } else {
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Account Creation Failed"
                                        com.siddharth.datamonitor.utils.AnalyticsHelper.logCrash("EmailCreateAccount", e)
                                    }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                            shape = RoundedCornerShape(4.dp), // Stark flat styling
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
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = "OR CONTINUE WITH",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Google Sign-In Button (Premium Multicolored Icon)
                            OutlinedButton(
                                onClick = {
                                    isLoading = true
                                    val signInIntent = googleSignInClient.signInIntent
                                    googleSignInLauncher.launch(signInIntent)
                                },
                                shape = RoundedCornerShape(4.dp), // Stark flat styling
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.siddharth.datamonitor.R.drawable.ic_google),
                                        contentDescription = "Google Icon",
                                        tint = Color.Unspecified, // Keep original high-quality vector multi-colors intact
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Google", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            // GitHub Sign-In Button
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
                                                com.siddharth.datamonitor.utils.AnalyticsHelper.logCrash("GitHubSignIn", e)
                                                if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                                    errorMessage = "Account exists. Please use the method you originally signed up with."
                                                    Toast.makeText(context, "Account exists. Please use the method you originally signed up with.", Toast.LENGTH_LONG).show()
                                                } else {
                                                    errorMessage = e.localizedMessage ?: "GitHub Federated Sync Failed"
                                                }
                                            }
                                    } else {
                                        errorMessage = "Internal Error: Active Activity missing."
                                    }
                                },
                                shape = RoundedCornerShape(4.dp), // Stark flat styling
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.siddharth.datamonitor.R.drawable.ic_github),
                                        contentDescription = "GitHub Icon",
                                        tint = MaterialTheme.colorScheme.onSurface, // Monochromatic for Vercel styling matching the theme
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("GitHub", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text(
                    text = "SKIP / CONTINUE AS GUEST →",
                )
            }
        }
    }
}
