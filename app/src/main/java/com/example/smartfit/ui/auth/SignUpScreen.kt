// app/src/main/java/com/example/smartfit/ui/auth/SignUpScreen.kt
package com.example.smartfit.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.common.RegisterBackground
import com.example.smartfit.ui.navigation.Dest

@Composable
fun SignUpScreen(
    navController: NavHostController,
    isDark: Boolean
) {
    val vm: SignUpViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val ui = vm.state.collectAsState().value

    // Navigate back to Login after successful sign up
    LaunchedEffect(Unit) {
        vm.effect.collect { eff ->
            if (eff is SignUpEffect.NavigateLogin) {
                navController.navigate(Dest.Login) {
                    popUpTo(Dest.SignUp) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val accent = Color(0xFFAEF200)
    val scroll = rememberScrollState()

    val canRegister =
        ui.name.isNotBlank() &&
                ui.email.isNotBlank() &&
                ui.password.length >= 6 &&
                ui.password == ui.confirm &&
                !ui.loading

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            // Blurred image background
            RegisterBackground(isDark)

            // Centered card-like column with max width
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 480.dp)      // LIMIT width on tablet
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                Text(
                    "Create",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Account",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(20.dp))

                GlassField(
                    value = ui.name,
                    onValueChange = vm::onName,
                    placeholder = "Name"
                )
                Spacer(Modifier.height(14.dp))
                GlassField(
                    value = ui.email,
                    onValueChange = vm::onEmail,
                    placeholder = "Email",
                    keyboardType = KeyboardType.Email
                )
                Spacer(Modifier.height(14.dp))
                GlassField(
                    value = ui.password,
                    onValueChange = vm::onPassword,
                    placeholder = "Password",
                    isPassword = true
                )
                Spacer(Modifier.height(14.dp))
                GlassField(
                    value = ui.confirm,
                    onValueChange = vm::onConfirm,
                    placeholder = "Confirm Password",
                    isPassword = true
                )

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = { vm.signUp() },
                    enabled = canRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),          // slightly smaller than before
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Color.Black,
                        disabledContainerColor = accent.copy(alpha = 0.5f),
                        disabledContentColor = Color.Black.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        if (ui.loading) "Creating..." else "Register",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                ui.error?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(18.dp))

                val login = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(
                        SpanStyle(
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("Login")
                    }
                }
                Text(
                    login,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        navController.navigate(Dest.Login)
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Glass-style text field used for the sign-up screen.
 * Matches the login screen styling and uses slightly smaller height & font.
 */
@Composable
private fun GlassField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp) // was 58.dp: slightly shorter
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), shape)
            .padding(horizontal = 14.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = {
                Text(
                    placeholder,
                    color = Color.White.copy(alpha = 0.65f)
                )
            },
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                fontSize = 16.sp // was 18.sp
            ),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color.White
            ),
            shape = shape,
            modifier = Modifier.fillMaxSize()
        )
    }
}
