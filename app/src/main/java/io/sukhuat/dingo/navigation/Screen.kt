package io.sukhuat.dingo.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Detail : Screen("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }
}
