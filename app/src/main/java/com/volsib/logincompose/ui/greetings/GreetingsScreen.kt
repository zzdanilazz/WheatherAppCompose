package com.volsib.logincompose.ui.greetings

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.volsib.logincompose.AppViewModelProvider
import com.volsib.logincompose.R
import com.volsib.logincompose.ui.navigation.NavigationDestination
import com.volsib.logincompose.ui.theme.LoginComposeTheme
import com.volsib.logincompose.ui.widget.WeatherWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


object GreetingsDestination : NavigationDestination {
    override val route = "greetings"
    override val titleRes = R.string.greetings_title
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GreetingsScreen(
    navigateToWeather: () -> Unit,
    viewModel: GreetingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val pagesCount = 2
    val pagerState = rememberPagerState { pagesCount }

    val coroutineScope = rememberCoroutineScope()

    // Проверяем наличие пользователя в репозитории
    LaunchedEffect(key1 = Unit) {
        if (viewModel.hasCurrentUser()) {
            // Если пользователь существует, переходим на WeatherScreen
            navigateToWeather()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Application logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            val tabs = listOf(
                stringResource(R.string.authentication),
                stringResource(R.string.registration)
            )
            tabs.forEachIndexed { index, tabText ->
                Tab(
                    text = { Text(tabText) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState
        ) { page: Int ->
            when (page) {
                0 -> {
                    viewModel.updatePageUiState(EntryType.AUTHENTICATION)
                    SignInPage(viewModel, coroutineScope, navigateToWeather)
                }
                1 -> {
                    viewModel.updatePageUiState(EntryType.REGISTRATION)
                    SignUpPage(viewModel, coroutineScope)
                }
            }
        }
    }
}

@Composable
fun SignInPage(
    viewModel: GreetingsViewModel,
    coroutineScope: CoroutineScope,
    navigateToWeather: () -> Unit
) {
    val context = LocalContext.current

    UserEntryBody(
        userUiState = viewModel.userUiState,
        pageUiState = viewModel.pageUiState,
        onUserValueChange = viewModel::updateUserUiState,
        onEntryActionClick = {
            coroutineScope.launch {
                val statusMessage = viewModel.authorizeUser()
                Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
                if (statusMessage == "Authorization successful") {
                    // Updating the app widget
                    WeatherWidget().updateAll(context)

                    // Navigating to the weather screen
                    navigateToWeather()
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun SignUpPage(
    viewModel: GreetingsViewModel,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current

    UserEntryBody(
        userUiState = viewModel.userUiState,
        pageUiState = viewModel.pageUiState,
        onUserValueChange = viewModel::updateUserUiState,
        onEntryActionClick = {
            coroutineScope.launch {
                val statusMessage = viewModel.registerUser()
                Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun UserEntryBody(
    userUiState: UserUiState,
    pageUiState: PageUiState,
    onUserValueChange: (UserDetails) -> Unit,
    onEntryActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        UserInputForm(
            entryType = pageUiState.entryType,
            userDetails = userUiState.userDetails,
            onValueChange = onUserValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        val buttonText = when (pageUiState.entryType) {
            EntryType.AUTHENTICATION -> stringResource(R.string.sign_in_action)
            EntryType.REGISTRATION -> stringResource(R.string.sign_up_action)
        }
        Button(
            onClick = onEntryActionClick,
            enabled = userUiState.isEntryValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = buttonText)
        }
    }
}

@Composable
fun UserInputForm(
    entryType: EntryType,
    userDetails: UserDetails,
    modifier: Modifier = Modifier,
    onValueChange: (UserDetails) -> Unit,
) {
    val maxFieldLength = 18

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        OutlinedTextField(
            value = userDetails.login,
            onValueChange = { onValueChange(userDetails.copy(login = it.take(maxFieldLength))) },
            label = { Text(stringResource(R.string.item_login)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = userDetails.password,
            onValueChange = { onValueChange(userDetails.copy(password = it.take(maxFieldLength))) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            label = { Text(stringResource(R.string.item_password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )

        if (entryType == EntryType.REGISTRATION) {
            OutlinedTextField(
                value = userDetails.confirmedPassword,
                onValueChange = { onValueChange(userDetails.copy(confirmedPassword = it.take(maxFieldLength))) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                label = { Text(stringResource(R.string.item_confirm_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemEntryScreenPreview() {
    LoginComposeTheme {
        UserEntryBody(
            pageUiState = PageUiState(entryType = EntryType.AUTHENTICATION),
            userUiState = UserUiState(
                UserDetails(
                    login = "danila228", password = "12345678"
                )
            ),
            onUserValueChange = {},
            onEntryActionClick = {}
        )
    }
}
