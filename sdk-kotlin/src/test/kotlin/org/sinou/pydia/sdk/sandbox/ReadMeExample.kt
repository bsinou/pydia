package org.sinou.pydia.sdk.sandbox

import org.junit.Assert
import org.junit.Test
import org.sinou.pydia.openapi.api.TreeServiceApi
import org.sinou.pydia.openapi.model.RestGetBulkMetaRequest
import org.sinou.pydia.sdk.api.Credentials
import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.transport.ServerURLImpl.Companion.fromAddress
import org.sinou.pydia.sdk.transport.auth.credentials.LegacyPasswordCredentials
import org.sinou.pydia.sdk.utils.Log
import org.sinou.pydia.sdk.utils.MemoryStore
import org.sinou.pydia.sdk.utils.tests.TestClientFactory
import org.sinou.pydia.sdk.utils.tests.TestCredentialService
import org.sinou.pydia.sdk.utils.tests.TestUtils
import java.net.MalformedURLException

class ReadMeExample {

    private val logTag = "ReadMeExample"

    private val defaultSlug = "common-files"

    @Test
    @Throws(SDKException::class, MalformedURLException::class)
    fun forTheReadMe() {

        val defaultParent = TestUtils.uniqueName("UnitTests")

        val factory = TestClientFactory(
            TestCredentialService(MemoryStore(), MemoryStore()),
            MemoryStore(), MemoryStore()
        )
        val serverURL = fromAddress("http://localhost:8080", true)
        val credentials: Credentials = LegacyPasswordCredentials("admin", "admin")
        val accountID = factory.registerAccountCredentials(serverURL, credentials)
        val transport = factory.getTransport(accountID)

        Assert.assertNotNull("No transport found for $accountID", transport)

        /* Use the simplified client*/
        val client = factory.getClient(transport!!)
        client.mkdir(defaultSlug, "/", defaultParent)
        client.mkdir(defaultSlug, "/$defaultParent", "child1")
        client.mkdir(defaultSlug, "/$defaultParent", "child2")
        client.mkdir(defaultSlug, "/$defaultParent", "child3")
        // This should not be created
        client.mkdir(defaultSlug, "/$defaultParent", "child3")

        // Directly use the generated API
        // Authentication is auto-magically handled by the transport as long as it has been previously registered
        val (url, okClient) = transport.apiConf()
        val api = TreeServiceApi(url, okClient)

        // Build the request
        val path = "$defaultSlug/$defaultParent/*"
        val request = RestGetBulkMetaRequest(
            nodePaths = listOf(path),
            allMetaProviders = true
        )

        // Performs the request and handle result
        val result = api.bulkStatNodes(request)
        Assert.assertEquals("Unexpected number of nodes found", 3, result.nodes?.size)
        Log.i(logTag, "Request done, found ${result.nodes?.size} nodes at $path:")
        result.nodes?.forEach {
            Log.i(logTag, " - UUID: ${it.uuid}, path: ${it.path} ")
        }

        // Delete the created nodes:
        client.delete(defaultSlug, arrayOf("/$defaultParent"), true)

        var found = false
        client.ls(slug = defaultSlug, path = "/", options = null) {
            if (it.name == defaultParent) {
                found = true
            }
        }
        Assert.assertEquals("Folder $defaultParent still exists", false, found)

        try {
            client.ls(slug = defaultSlug, path = "/$defaultParent", options = null) {
                Log.e(logTag, "Found unexpected node: UUID: ${it.id}, path: ${it.path} ")
            }
        } catch (e: Exception) { // expected
            e.printStackTrace()
        }

    }
}