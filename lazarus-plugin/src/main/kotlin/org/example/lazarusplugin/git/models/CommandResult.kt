package org.example.lazarusplugin.git.models

data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)