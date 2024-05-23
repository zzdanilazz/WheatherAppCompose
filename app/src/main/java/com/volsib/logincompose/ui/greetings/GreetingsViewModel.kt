package com.volsib.logincompose.ui.greetings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.volsib.logincompose.data.User
import com.volsib.logincompose.data.UserRepository
import kotlinx.coroutines.flow.firstOrNull

class GreetingsViewModel(private val userRepository: UserRepository) : ViewModel() {
    /**
     * Holds current user ui state
     */
    var userUiState by mutableStateOf(UserUiState())
        private set

    var pageUiState by mutableStateOf(PageUiState())
        private set

    fun updatePageUiState(entryType: EntryType) {
        pageUiState = PageUiState(
            entryType = entryType
        )
        updateUserUiState(userUiState.userDetails)
    }

    /**
     * Updates the [userUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUserUiState(
        userDetails: UserDetails
    ) {
        userUiState = UserUiState(
            userDetails = userDetails,
            isEntryValid = validateInput(pageUiState.entryType, userDetails)
        )
    }

    suspend fun registerUser(): String {
        return if (validateInput()) {
            val currentUser = userUiState.userDetails.toUser()

            val existingUserStream = userRepository.getUserByLoginStream(currentUser.login)
            val existingUser = existingUserStream.firstOrNull()

            if (existingUser == null) {
                userRepository.insertUser(currentUser)
                "Registration completed successfully"
            } else {
                "A user with this login already exists"
            }
        } else {
            "The login and password should not be empty"
        }
    }

    suspend fun hasCurrentUser(): Boolean {
        val currentUser = userRepository.getCurrentUserStream().firstOrNull()
        return currentUser != null
    }

    suspend fun authorizeUser(): String {
        return if (validateInput()) {
            val currentUser = userUiState.userDetails.toUser()

            val existingUserStream = userRepository.getUserByLoginStream(currentUser.login)
            val existingUser = existingUserStream.firstOrNull()

            if (existingUser != null && currentUser.password == existingUser.password) {
                existingUser.isSignedIn = true
                userRepository.updateUser(existingUser)
                "Authorization successful"
            } else {
                "Invalid login or password"
            }
        } else {
            "The login and password should not be empty"
        }
    }

    private fun validateInput(
        entryType: EntryType = pageUiState.entryType,
        userDetails: UserDetails = userUiState.userDetails
    ): Boolean {
        return with(userDetails) {
            when (entryType) {
                EntryType.AUTHENTICATION -> login.isNotBlank() && password.isNotBlank()
                EntryType.REGISTRATION ->
                    login.isNotBlank()
                            && password.isNotBlank()
                            && confirmedPassword.isNotBlank()
                            && password==confirmedPassword
            }
        }
    }
}

data class PageUiState(
    val entryType: EntryType = EntryType.AUTHENTICATION
)

/**
 * Represents Ui State for a User.
 */
data class UserUiState(
    val userDetails: UserDetails = UserDetails(),
    val isEntryValid: Boolean = false
)

enum class EntryType {
    AUTHENTICATION,
    REGISTRATION
}

data class UserDetails(
    val id: Int = 0,
    val login: String = "",
    val password: String = "",
    val confirmedPassword: String = ""
)

/**
 * Extension function to convert [UserUiState] to [User].
 */
fun UserDetails.toUser(): User = User(
    id = id,
    login = login,
    password = password
)

/**
 * Extension function to convert [User] to [UserUiState]
 */
fun User.toUserUiState(isEntryValid: Boolean = false): UserUiState = UserUiState(
    userDetails = this.toUserDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [User] to [UserDetails]
 */
fun User.toUserDetails(): UserDetails = UserDetails(
    id = id,
    login = login,
    password = password,
    confirmedPassword = ""
)