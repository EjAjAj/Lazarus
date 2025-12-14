package org.example.lazarusplugin.models

data class FileReport(
    val filePath: String,
    val connectedFiles: List<String>,
    val connectionDescriptions: List<String>
)