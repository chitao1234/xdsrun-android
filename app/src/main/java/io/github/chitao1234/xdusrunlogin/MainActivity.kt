package io.github.chitao1234.xdusrunlogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.chitao1234.xdusrunlogin.core.Logger
import io.chitao1234.xdusrunlogin.core.Core
import io.github.chitao1234.xdusrunlogin.ui.theme.XDUSRUNLoginTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.chitao1234.xdusrunlogin.data.Credential
import io.github.chitao1234.xdusrunlogin.data.CredStore
import io.github.chitao1234.xdusrunlogin.ui.login.CredentialActionsRow
import io.github.chitao1234.xdusrunlogin.ui.login.CredentialFields
import io.github.chitao1234.xdusrunlogin.ui.login.LoginTopBar
import io.github.chitao1234.xdusrunlogin.ui.login.LogsSection
import io.github.chitao1234.xdusrunlogin.ui.login.PrimaryActionsRow
import io.github.chitao1234.xdusrunlogin.ui.login.SavedCredentialsDropdown
import io.github.chitao1234.xdusrunlogin.ui.login.StatusText

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XDUSRUNLoginTheme {
                LoginScreen()
            }
        }
    }
}

 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val logs = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var savedCreds by remember { mutableStateOf<List<Credential>>(emptyList()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var autoLogin by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
		Core.setLogger(object : Logger {
			override fun log(p0: String) {
				val line = p0
				scope.launch(Dispatchers.Main) {
					logs.add(line.trimEnd('\n'))
				}
			}
		})
		onDispose { }
	}

    // Load saved credentials and auto-login settings; trigger auto-login if enabled
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val list = CredStore.load(context)
        savedCreds = list
        autoLogin = CredStore.getAutoLogin(context)
        if (!autoLogin) {
            return@LaunchedEffect
        }
        val defId = CredStore.getDefaultId(context)
        if (defId == null) {
            status = context.getString(R.string.no_default_credential_set)
            return@LaunchedEffect
        } 
        selectedId = defId
        val def = list.firstOrNull { it.id == defId } ?: return@LaunchedEffect
        username = def.username
        password = def.password
        domain = def.domain
        status = context.getString(R.string.auto_login_requested)
        scope.launch(Dispatchers.IO) {
            try {
                val loggedIn = Core.checkStatusWithDefaultClient()
                if (loggedIn) {
                    status = context.getString(R.string.already_logged_in)
                    return@launch
                }
                val ret = Core.performLoginWithDefaultClient(
                    def.username,
                    def.password,
                    def.domain
                )
                if (ret) {
                    status = context.getString(R.string.auto_login_successful)
                } else {
                    status = context.getString(R.string.auto_login_failed)
                }
            } catch (t: Throwable) {
                status = context.getString(R.string.auto_login_error_with_message, t.message)
            }
        }
    }

    Scaffold(topBar = {
        LoginTopBar(
            autoLogin = autoLogin,
            onToggleAutoLogin = { enabled ->
                autoLogin = enabled
                CredStore.setAutoLogin(context, enabled)
            }
        )
    }) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SavedCredentialsDropdown(
                    savedCreds = savedCreds,
                    selectedId = selectedId,
                    onSelect = { cred ->
                        selectedId = cred.id
                        username = cred.username
                        password = cred.password
                        domain = cred.domain
                    }
                )
                CredentialFields(
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it },
                    domain = domain,
                    onDomainChange = { domain = it }
                )
            }
            PrimaryActionsRow(
                onCheckStatus = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val loggedIn = Core.checkStatusWithDefaultClient()
                            status = context.getString(R.string.logged_in_with_value, loggedIn.toString())
                        } catch (t: Throwable) {
                            status = context.getString(R.string.error_with_message, t.message)
                        }
                    }
                },
                onLogin = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val ret = Core.performLoginWithDefaultClient(username, password, domain)
                            if (ret) {
                                status = context.getString(R.string.login_successful)
                            } else {
                                status = context.getString(R.string.login_failed)
                            }
                        } catch (t: Throwable) {
                            status = context.getString(R.string.error_with_message, t.message)
                        }
                    }
                }
            )
            CredentialActionsRow(
                enabled = selectedId != null,
                onSave = {
                    scope.launch(Dispatchers.IO) {
                        val c = CredStore.saveOrUpdate(context, username, password, domain)
                        val list = CredStore.load(context)
                        scope.launch(Dispatchers.Main) {
                            savedCreds = list
                            selectedId = c.id
                            status = context.getString(R.string.credential_saved)
                        }
                    }
                },
                onSetDefault = {
                    val id = selectedId
                    if (id != null) {
                        CredStore.setDefaultId(context, id)
                        status = context.getString(R.string.default_credential_set)
                    }
                },
                onRemove = {
                    val id = selectedId
                    if (id != null) {
                        scope.launch(Dispatchers.IO) {
                            CredStore.removeById(context, id)
                            val list = CredStore.load(context)
                            scope.launch(Dispatchers.Main) {
                                savedCreds = list
                                if (list.none { it.id == id }) {
                                    selectedId = null
                                }
                                status = context.getString(R.string.credential_removed)
                            }
                        }
                    }
                }
            )
            if (status.isNotEmpty()) {
                StatusText(status = status)
            }
            LogsSection(
                logs = logs,
                onClear = { logs.clear() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    XDUSRUNLoginTheme {
        LoginScreen()
    }
}