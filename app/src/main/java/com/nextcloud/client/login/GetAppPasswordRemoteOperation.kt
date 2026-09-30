/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package com.nextcloud.client.login

import com.nextcloud.common.NextcloudClient
import com.nextcloud.operations.GetMethod
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection

/**
 * Exchanges the login name and password that the client was built with for a new app password.
 */
class GetAppPasswordRemoteOperation : RemoteOperation<String>() {

    override fun run(client: NextcloudClient): RemoteOperationResult<String> {
        val getMethod = GetMethod(client.baseUri.toString() + ENDPOINT + JSON_FORMAT, true)

        return try {
            val status = client.execute(getMethod)
            if (status != HttpURLConnection.HTTP_OK) {
                return RemoteOperationResult(false, getMethod)
            }

            val appPassword = JSONObject(getMethod.getResponseBodyAsString())
                .getJSONObject(NODE_OCS)
                .getJSONObject(NODE_DATA)
                .getString(NODE_APP_PASSWORD)

            RemoteOperationResult<String>(true, getMethod).apply { resultData = appPassword }
        } catch (e: IOException) {
            Log_OC.e(TAG, "Getting app password failed", e)
            RemoteOperationResult(e)
        } catch (e: JSONException) {
            Log_OC.e(TAG, "Parsing app password failed", e)
            RemoteOperationResult(e)
        } finally {
            getMethod.releaseConnection()
        }
    }

    companion object {
        private val TAG = GetAppPasswordRemoteOperation::class.java.simpleName
        private const val ENDPOINT = "/ocs/v2.php/core/getapppassword"
        private const val NODE_OCS = "ocs"
        private const val NODE_DATA = "data"
        private const val NODE_APP_PASSWORD = "apppassword"
    }
}
