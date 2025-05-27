package com.yagosouza.crudcomposeapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.yagosouza.crudcomposeapp.ui.screen.home.HomeScreen
import com.yagosouza.crudcomposeapp.ui.screen.itemlist.ItemListScreen
import com.yagosouza.crudcomposeapp.ui.screen.itemlist.ItemListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel // Importação correta

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Início", Icons.Filled.Home)
    object ItemList : Screen("itemList", "Lista de Itens", Icons.Filled.List)
}

val navigationItems = listOf(
    Screen.Home,
    Screen.ItemList
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentTitle by remember { mutableStateOf(Screen.Home.title) }

    navController.addOnDestinationChangedListener { _, destination, _ ->
        currentTitle = navigationItems.find { it.route == destination.route }?.title ?: "CRUD App"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                Divider()
                navigationItems.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = screen.route == navController.currentBackStackEntryAsState().value?.destination?.route,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) { HomeScreen(navController) }
                composable(Screen.ItemList.route) {
                    // Use koinViewModel para obter a instância do ViewModel
                    val itemListViewModel: ItemListViewModel = koinViewModel()
                    ItemListScreen(navController, itemListViewModel)
                }
            }
        }
    }
}