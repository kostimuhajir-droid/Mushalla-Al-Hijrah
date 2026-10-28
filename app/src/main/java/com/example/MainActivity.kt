package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.model.TransactionType
import com.example.data.repository.MushallaRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MushallaViewModel
import com.example.ui.viewmodel.MushallaViewModelFactory

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    DASHBOARD("Beranda", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    KAS("Buku Kas", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_kas"),
    WAKAF("Wakaf", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "nav_wakaf"),
    INVENTARIS("Inventaris", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_inventaris"),
    LAPORAN("Laporan", Icons.Filled.Description, Icons.Outlined.Description, "nav_laporan"),
    PROFIL("Profil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profil")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MushallaViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = MushallaRepository(
            kasDao = db.kasDao(),
            wakafDao = db.wakafDao(),
            inventoryDao = db.inventoryDao(),
            profileDao = db.profileDao()
        )
        MushallaViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MushallaViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    // Quick trigger flags for dialogs
    var openKasDialogOnNav by remember { mutableStateOf(false) }
    var kasDialogType by remember { mutableStateOf(TransactionType.MASUK) }
    var openWakafDialogOnNav by remember { mutableStateOf(false) }

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    // Handle back button: return to Dashboard if on another screen
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = profile?.name ?: "Mushalla Ar-Rahman",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Enkripsi",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sistem Manajemen Keuangan Syariah",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentScreen = AppScreen.PROFIL },
                        modifier = Modifier.testTag("top_profile_button")
                    ) {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.PROFIL) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                            contentDescription = "Profil Mushalla",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                listOf(
                    AppScreen.DASHBOARD,
                    AppScreen.KAS,
                    AppScreen.WAKAF,
                    AppScreen.INVENTARIS,
                    AppScreen.LAPORAN
                ).forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentScreen = screen
                            openKasDialogOnNav = false
                            openWakafDialogOnNav = false
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToKas = { currentScreen = AppScreen.KAS },
                        onNavigateToWakaf = { currentScreen = AppScreen.WAKAF },
                        onNavigateToInventory = { currentScreen = AppScreen.INVENTARIS },
                        onNavigateToReports = { currentScreen = AppScreen.LAPORAN },
                        onNavigateToProfile = { currentScreen = AppScreen.PROFIL },
                        onQuickAddKasMasuk = {
                            kasDialogType = TransactionType.MASUK
                            openKasDialogOnNav = true
                            currentScreen = AppScreen.KAS
                        },
                        onQuickAddKasKeluar = {
                            kasDialogType = TransactionType.KELUAR
                            openKasDialogOnNav = true
                            currentScreen = AppScreen.KAS
                        },
                        onQuickAddWakaf = {
                            openWakafDialogOnNav = true
                            currentScreen = AppScreen.WAKAF
                        }
                    )
                }

                AppScreen.KAS -> {
                    KasScreen(
                        viewModel = viewModel,
                        initialOpenAddDialog = openKasDialogOnNav,
                        initialTransactionType = kasDialogType
                    )
                }

                AppScreen.WAKAF -> {
                    WakafScreen(
                        viewModel = viewModel,
                        initialOpenAddDialog = openWakafDialogOnNav
                    )
                }

                AppScreen.INVENTARIS -> {
                    InventoryScreen(viewModel = viewModel)
                }

                AppScreen.LAPORAN -> {
                    ReportsScreen(viewModel = viewModel)
                }

                AppScreen.PROFIL -> {
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}
