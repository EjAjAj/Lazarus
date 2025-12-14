package org.example.lazarusplugin.services.git.api

import org.example.lazarusplugin.models.git.FileChange

interface IDiffService {
    fun getChangedFiles(): List<FileChange>
    fun getChangedFilesWithDiffs(): Map<String, String>
}