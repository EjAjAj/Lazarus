package org.example.lazarusplugin.services.api

import org.example.lazarusplugin.models.*

/**
 * Interface for querying and reading from the code graph
 */
interface GraphReader {

    /**
     * Get facts about a specific file
     */
    fun getFileFacts(filePath: String, relevantFiles: Array<String>): FileReport
}
