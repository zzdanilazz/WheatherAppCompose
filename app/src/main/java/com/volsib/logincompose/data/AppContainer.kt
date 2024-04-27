package com.volsib.logincompose.data

import android.content.Context

interface AppContainer {
    val userRepository: UserRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val userRepository: UserRepository by lazy {
        OfflineUsersRepository(ApplicationDatabase.getDatabase(context).userDao())
    }
}