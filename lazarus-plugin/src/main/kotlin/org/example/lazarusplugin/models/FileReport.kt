package org.example.lazarusplugin.models

data class FileReport(
    val filePath: String,
    val connectedFiles: ArrayList<String>,
    val connectionDescriptions: ArrayList<String>
)