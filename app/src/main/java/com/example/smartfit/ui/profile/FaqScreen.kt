package com.example.smartfit.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.SmartFitTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.KeyboardArrowDown



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onNavigateUp: () -> Unit) {

    val colorScheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQ") },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .widthIn(max = 700.dp)
            ) {

                ExpandableFaq(
                    question = "How do I add a new activity log?",
                    answer = "From the main 'Logs' screen..."
                )

                ExpandableFaq(
                    question = "Can I edit my profile information?",
                    answer = "Yes. Navigate to the 'Profile' tab..."
                )

                ExpandableFaq(
                    question = "How is my data stored?",
                    answer = "All your data is stored locally..."
                )

                ExpandableFaq(
                    question = "Is my password secure?",
                    answer = "Your password is hashed using SHA-256..."
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ExpandableFaq(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
private fun FaqScreenPreview() {
    SmartFitTheme {
        FaqScreen(onNavigateUp = {})
    }
}
