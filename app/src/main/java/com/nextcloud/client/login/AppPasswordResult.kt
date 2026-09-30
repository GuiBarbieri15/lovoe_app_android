/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.nextcloud.client.login

import java.net.HttpURLConnection

sealed interface AppPasswordResult {
    data class Success(val appPassword: String) : AppPasswordResult

    /**
     * The server answers 401 both for a wrong password and for accounts that require two-factor authentication,
     * so the two cases cannot be told apart.
     */
    data object InvalidCredentials : AppPasswordResult

    data object Failure : AppPasswordResult

    companion object {
        @JvmStatic
        fun fromResponse(httpCode: Int, generatedPassword: String?, enteredPassword: String): AppPasswordResult =
            when (httpCode) {
                HttpURLConnection.HTTP_OK ->
                    if (generatedPassword.isNullOrEmpty()) Failure else Success(generatedPassword)

                // The server refuses to derive an app password from a password that already is one.
                HttpURLConnection.HTTP_FORBIDDEN -> Success(enteredPassword)

                HttpURLConnection.HTTP_UNAUTHORIZED -> InvalidCredentials

                else -> Failure
            }
    }
}
