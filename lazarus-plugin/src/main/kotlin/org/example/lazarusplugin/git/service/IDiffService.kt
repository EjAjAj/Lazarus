package org.example.lazarusplugin.git.service

import org.example.lazarusplugin.git.models.FileChange

interface IDiffService {
    fun getChangedFiles(): List<FileChange>
    fun getChangedFilesWithDiffs(): Map<String, String>
}