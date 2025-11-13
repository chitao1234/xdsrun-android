package io.github.chitao1234.xdusrunlogin.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.chitao1234.xdusrunlogin.data.Credential
import io.github.chitao1234.xdusrunlogin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTopBar(
    autoLogin: Boolean,
    onToggleAutoLogin: (Boolean) -> Unit
) {
    var appMenuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(onClick = { appMenuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.menu))
            }
            DropdownMenu(
                expanded = appMenuExpanded,
                onDismissRequest = { appMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.auto_login_on_launch)) },
                    trailingIcon = {
                        Switch(
                            checked = autoLogin,
                            onCheckedChange = { onToggleAutoLogin(it) }
                        )
                    },
                    onClick = {
                        onToggleAutoLogin(!autoLogin)
                        appMenuExpanded = false
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCredentialsDropdown(
    savedCreds: List<Credential>,
    selectedId: String?,
    onSelect: (Credential) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        val selected = savedCreds.firstOrNull { it.id == selectedId }
        val display = selected?.let { "${it.username}${it.domain}" } ?: ""
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.saved_credentials)) },
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            savedCreds.forEach { cred ->
                DropdownMenuItem(
                    text = { Text("${cred.username}${cred.domain}") },
                    onClick = {
                        onSelect(cred)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CredentialFields(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    domain: String,
    onDomainChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            singleLine = true,
            label = { Text(stringResource(R.string.username)) },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            singleLine = true,
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password))
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = domain,
            onValueChange = onDomainChange,
            singleLine = true,
            label = { Text(stringResource(R.string.domain)) },
            leadingIcon = { Icon(Icons.Filled.Public, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PrimaryActionsRow(
    onCheckStatus: () -> Unit,
    onLogin: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.Start) {
        Button(onClick = onCheckStatus) {
            Text(stringResource(R.string.check_status))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(onClick = onLogin) {
            Text(stringResource(R.string.login))
        }
    }
}

@Composable
fun CredentialActionsRow(
    enabled: Boolean,
    onSave: () -> Unit,
    onSetDefault: () -> Unit,
    onRemove: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.Start) {
        Button(onClick = onSave) {
            Text(stringResource(R.string.save_credential))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            enabled = enabled,
            onClick = onSetDefault
        ) {
            Text(stringResource(R.string.set_default))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            enabled = enabled,
            onClick = onRemove
        ) {
            Text(stringResource(R.string.remove))
        }
    }
}

@Composable
fun StatusText(status: String) {
    Text(text = status, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun LogsSection(
    logs: List<String>,
    onClear: () -> Unit
) {
    Row {
        Text(text = stringResource(R.string.logs), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        TextButton(onClick = onClear) {
            Text(stringResource(R.string.clear))
        }
    }
    LazyColumn {
        items(logs) { line ->
            Text(text = line, style = MaterialTheme.typography.bodySmall)
        }
    }
}


