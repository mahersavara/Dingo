package io.sukhuat.dingo.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object ProfileEdit : Screen("profile/edit")
    data object ProfileStatistics : Screen("profile/statistics")
    data object ProfileAccount : Screen("profile/account")
    data object ProfileHelp : Screen("profile/help")
    data object Detail : Screen("detail/{id}") {
        fun createRoute(id: Int) = "detail/$id"
    }

    // Year Planner routes
    data object YearPlannerCurrent : Screen("year_planner")
    data object YearPlanner : Screen("year_planner/{year}") {
        fun createRoute(year: Int) = "year_planner/$year"
    }
}
