package org.example.lazarusplugin.services.api

import org.example.lazarusplugin.models.*

/**
 * Interface for querying and reading from the code graph
 */
data class FileReport(
    val filePath: String,
    val connectedFiles: ArrayList<String>,
    val connectionDescriptions: ArrayList<String>
)

interface GraphReader {

    /**
     * Get facts about a specific file
     */
    fun getFileFacts(filePath: String, relevantFiles: Array<String>): FileReport
}
