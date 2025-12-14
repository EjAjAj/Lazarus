package org.example.lazarusplugin.models.git

data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)