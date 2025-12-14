package org.example.lazarusplugin.services.api

/**
 * Interface for querying and reading from the code graph
 */
interface IGraphReader {

    /**
     * Get facts about a specific file
     */
    fun getFileFacts(filePath: String): Array<String>
}
