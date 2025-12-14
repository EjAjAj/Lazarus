package org.example.lazarusplugin.git.service

import org.example.lazarusplugin.git.models.FileChange

interface IDiffRepository {
    fun getDiff(fromRef: String, toRef: String, filePath: String? = null): String
    fun getNameStatus(fromRef: String, toRef: String): List<FileChange>
    fun getStatus(): List<FileChange>
}