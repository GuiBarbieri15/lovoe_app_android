/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.nextcloud.client.login

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.HttpURLConnection

class AppPasswordResultTest {

    companion object {
        private const val GENERATED_PASSWORD = "generated-app-password"
        private const val ENTERED_PASSWORD = "entered-password"
        private const val NETWORK_ERROR_CODE = -1
    }

    @Test
    fun okWithPasswordReturnsGeneratedPassword() {
        val result = AppPasswordResult.fromResponse(HttpURLConnection.HTTP_OK, GENERATED_PASSWORD, ENTERED_PASSWORD)

        assertEquals(AppPasswordResult.Success(GENERATED_PASSWORD), result)
    }

    @Test
    fun okWithoutPasswordIsFailure() {
        val result = AppPasswordResult.fromResponse(HttpURLConnection.HTTP_OK, null, ENTERED_PASSWORD)

        assertEquals(AppPasswordResult.Failure, result)
    }

    @Test
    fun forbiddenReusesEnteredAppPassword() {
        val result = AppPasswordResult.fromResponse(HttpURLConnection.HTTP_FORBIDDEN, null, ENTERED_PASSWORD)

        assertEquals(AppPasswordResult.Success(ENTERED_PASSWORD), result)
    }

    @Test
    fun unauthorizedIsInvalidCredentials() {
        val result = AppPasswordResult.fromResponse(HttpURLConnection.HTTP_UNAUTHORIZED, null, ENTERED_PASSWORD)

        assertEquals(AppPasswordResult.InvalidCredentials, result)
    }

    @Test
    fun networkErrorIsFailure() {
        val result = AppPasswordResult.fromResponse(NETWORK_ERROR_CODE, null, ENTERED_PASSWORD)

        assertEquals(AppPasswordResult.Failure, result)
    }
}
