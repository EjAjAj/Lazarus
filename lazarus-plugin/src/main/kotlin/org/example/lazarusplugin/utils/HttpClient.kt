package org.example.lazarusplugin.utils

import com.intellij.openapi.components.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service(Service.Level.APP)
class HttpClient(
    private val baseUrl: String = "http://localhost:8080",
    private val apiKey: String? = null
) {
    private val client = HttpClient.newHttpClient()

    fun post(endpoint: String, body: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$endpoint"))
            .header("Content-Type", "application/json")
            .apply { apiKey?.let { header("Authorization", "Bearer $it") } }
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun get(endpoint: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$endpoint"))
            .apply { apiKey?.let { header("Authorization", "Bearer $it") } }
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}