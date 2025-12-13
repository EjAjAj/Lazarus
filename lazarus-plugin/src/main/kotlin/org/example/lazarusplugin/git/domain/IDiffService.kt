package org.example.lazarusplugin.git.domain

import org.example.lazarusplugin.git.models.FileChange

interface IDiffService {
    fun getChangedFiles(): List<FileChange>
    fun getChangedFilesWithDiffs(): Map<String, String>
}