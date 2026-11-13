package com.example.minecraftclicker.ui

import com.example.minecraftclicker.model.Tab

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"

    val publicRoutes = setOf(LOGIN, SIGN_UP)

    val tabRoutes: Set<String> = Tab.entries.map { it.route }.toSet()
}
