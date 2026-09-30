/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.nextcloud.client.login

import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.nextcloud.common.NextcloudClient
import com.owncloud.android.R
import com.owncloud.android.authentication.AuthenticatorActivity
import com.owncloud.android.authentication.LoginUrlInfo
import com.owncloud.android.databinding.AccountSetupNativeLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials

/**
 * Login screen with login name and password fields, used instead of the browser based Login Flow v2.
 * It does not support two-factor authentication or external identity providers, which is why it offers the
 * browser login as a fallback.
 */
class NativeLoginForm(private val activity: AuthenticatorActivity, private val serverUrl: String) {

    private val binding = AccountSetupNativeLoginBinding.inflate(activity.layoutInflater)

    fun show() {
        activity.setContentView(binding.root)

        binding.loginButton.setOnClickListener { submit() }
        binding.passwordInput.setOnEditorActionListener { _, actionId, _ ->
            val isDone = actionId == EditorInfo.IME_ACTION_DONE
            if (isDone) {
                submit()
            }
            isDone
        }
        binding.browserLoginButton.setOnClickListener { activity.startBrowserLogin() }
    }

    fun showError(@StringRes message: Int) {
        setLoading(false)
        binding.errorText.setText(message)
        binding.errorText.visibility = View.VISIBLE
    }

    private fun submit() {
        val loginName = binding.usernameInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        if (loginName.isEmpty() || password.isEmpty()) {
            showError(R.string.native_login_missing_credentials)
            return
        }

        setLoading(true)
        activity.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { requestAppPassword(loginName, password) }
            onAppPasswordResult(loginName, result)
        }
    }

    private fun requestAppPassword(loginName: String, password: String): AppPasswordResult {
        val credentials = Credentials.basic(loginName, password, Charsets.UTF_8)
        val client = NextcloudClient(serverUrl.toUri(), loginName, credentials, activity)
        val result = client.execute(GetAppPasswordRemoteOperation())
        return AppPasswordResult.fromResponse(result.httpCode, result.resultData, password)
    }

    private fun onAppPasswordResult(loginName: String, result: AppPasswordResult) {
        when (result) {
            is AppPasswordResult.Success -> activity.login(LoginUrlInfo(serverUrl, loginName, result.appPassword))
            AppPasswordResult.InvalidCredentials -> showError(R.string.native_login_invalid_credentials)
            AppPasswordResult.Failure -> showError(R.string.native_login_connection_error)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.browserLoginButton.isEnabled = !isLoading
        binding.usernameInput.isEnabled = !isLoading
        binding.passwordInput.isEnabled = !isLoading
        binding.loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.errorText.visibility = View.GONE
        }
    }
}
