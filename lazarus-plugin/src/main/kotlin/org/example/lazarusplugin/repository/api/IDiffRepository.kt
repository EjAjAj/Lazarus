package org.example.lazarusplugin.repository.api

import org.example.lazarusplugin.models.git.FileChange

interface IDiffRepository {
    fun getDiff(fromRef: String, toRef: String, filePath: String? = null): String
    fun getNameStatus(fromRef: String, toRef: String): List<FileChange>
    fun getStatus(): List<FileChange>
}