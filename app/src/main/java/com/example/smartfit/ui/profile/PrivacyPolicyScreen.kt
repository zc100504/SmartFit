package com.example.smartfit.ui.profile


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.SmartFitTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateUp: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->

        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Adaptive reading width
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .widthIn(max = 700.dp)   // adaptive for tablet
            ) {

                InfoCardSection(
                    title = "Information We Collect",
                    content = "SmartFit collects data you provide when you create an account ..."
                )

                InfoCardSection(
                    title = "How We Use Your Information",
                    content = "Your data is used to provide and improve the SmartFit experience ..."
                )

                InfoCardSection(
                    title = "Data Security",
                    content = "We take reasonable measures to protect your information ..."
                )

                InfoCardSection(
                    title = "Your Control",
                    content = "You can access, edit, or delete your personal information ..."
                )

                InfoCardSection(
                    title = "Policy Updates",
                    content = "We may update this privacy policy from time to time ..."
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoCardSection(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyPolicyScreenPreview() {
    SmartFitTheme {
        PrivacyPolicyScreen(onNavigateUp = {})
    }
}
