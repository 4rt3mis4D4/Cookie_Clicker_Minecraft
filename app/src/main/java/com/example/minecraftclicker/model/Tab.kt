package com.example.minecraftclicker.model

enum class Tab(val route: String, val label: String, val adminOnly: Boolean = false) {
    CLICKER("clicker", "Minerar"),
    UPGRADES("upgrades", "Upgrades"),
    RANKING("ranking", "Ranking"),
    STATS("stats", "Estatísticas"),
    ADMIN("admin", "Admin", adminOnly = true)
}
