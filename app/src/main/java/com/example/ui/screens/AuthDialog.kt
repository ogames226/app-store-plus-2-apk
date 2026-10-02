package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserProfile
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onSignIn: suspend (String, String) -> UserProfile?,
    onSignUp: suspend (String, String, String) -> UserProfile?,
    onGoogleSignIn: suspend (String, String) -> UserProfile?,
    onAuthSuccess: (UserProfile) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("zaim9002@gmail.com") }
    var password by remember { mutableStateOf("123456") }
    var name by remember { mutableStateOf("Admin") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Column {
                TabRow(
                    selectedTabIndex = tabIndex,
                    containerColor = SurfaceCard,
                    contentColor = BrandBlue
                ) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text("تسجيل الدخول", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("حساب جديد", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Google Sign In Button
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val user = onGoogleSignIn(email.ifBlank { "zaim9002@gmail.com" }, name.ifBlank { "Admin" })
                            isLoading = false
                            if (user != null) {
                                onAuthSuccess(user)
                                onDismiss()
                            } else {
                                errorMessage = "تعذر تسجيل الدخول بحساب Google"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceVariantDark)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_chrome),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "تسجيل الدخول بحساب Google",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    Text("أو عبر البريد", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (tabIndex == 1) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = BrandRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Admin accounts fast selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        email = "zaim9002@gmail.com"
                        password = "password123"
                        name = "Zaim Admin"
                    }) {
                        Text("المدير: zaim9002", fontSize = 11.sp, color = BrandBlue, fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = {
                        email = "ogames226@gmail.com"
                        password = "password123"
                        name = "OGames Admin"
                    }) {
                        Text("مشرف: ogames226", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val user = if (tabIndex == 0) {
                            onSignIn(email, password)
                        } else {
                            onSignUp(email, password, name)
                        }
                        isLoading = false
                        if (user != null) {
                            onAuthSuccess(user)
                            onDismiss()
                        } else {
                            errorMessage = "فشل تسجيل الدخول. يرجى التحقق من البيانات."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(if (tabIndex == 0) "دخول" else "إنشاء حساب", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}
