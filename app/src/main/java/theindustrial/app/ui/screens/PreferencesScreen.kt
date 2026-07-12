package theindustrial.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import theindustrial.app.data.local.PreferenceManager
import theindustrial.app.data.model.PreferenceItem
import theindustrial.app.data.remote.RetrofitInstance
import theindustrial.app.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val preferenceManager = remember { PreferenceManager(context) }
    val appKey by preferenceManager.appKey.collectAsState(initial = null)
    val userId = ThemeManager.userId.value
    val scope = rememberCoroutineScope()

    var newPreference by remember { mutableStateOf("") }
    var preferencesList by remember { mutableStateOf<List<PreferenceItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isAdding by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load existing preferences
    LaunchedEffect(appKey, userId) {
        if (!appKey.isNullOrBlank() && userId != null) {
            isLoading = true
            try {
                val response = RetrofitInstance.api.viewPreferences(appKey!!.trim(), userId)
                if (response.isSuccessful) {
                    preferencesList = response.body()?.responseDetails ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load preferences"
            } finally {
                isLoading = false
            }
        }
    }

    val addPref = {
        if (newPreference.isNotBlank() && userId != null && !appKey.isNullOrBlank()) {
            scope.launch {
                isAdding = true
                try {
                    val cleanKey = appKey!!.trim()
                    val keyword = newPreference.trim()
                    val response = RetrofitInstance.api.addPreference(
                        appKey = cleanKey, userId = userId, keyword = keyword,
                        appKeyQ = cleanKey, userIdQ = userId, keywordQ = keyword
                    )
                    if (response.isSuccessful) {
                        preferencesList = response.body()?.responseDetails ?: emptyList()
                        newPreference = ""
                        focusManager.clearFocus()
                    }
                } catch (e: Exception) {
                    errorMessage = "Failed to add preference"
                } finally {
                    isAdding = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Input Area
        OutlinedTextField(
            value = newPreference,
            onValueChange = { newPreference = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Add new topic (e.g. Automation)", fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { addPref() }, enabled = newPreference.isNotBlank()) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { addPref() })
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Topics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(preferencesList) { item ->
                    PreferenceChip(
                        label = item.keyword ?: "",
                        onRemove = {
                            scope.launch {
                                try {
                                    val cleanKey = appKey!!.trim()
                                    val response = RetrofitInstance.api.removePreference(
                                        appKey = cleanKey, userId = userId!!, prefId = item.id,
                                        appKeyQ = cleanKey, userIdQ = userId!!, prefIdQ = item.id
                                    )
                                    if (response.isSuccessful) {
                                        preferencesList = response.body()?.responseDetails ?: emptyList()
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to remove"
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PreferenceChip(label: String, onRemove: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
