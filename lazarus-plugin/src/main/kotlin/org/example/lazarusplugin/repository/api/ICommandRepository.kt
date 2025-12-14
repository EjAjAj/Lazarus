package org.example.lazarusplugin.git.service

import org.example.lazarusplugin.git.models.CommandResult

interface ICommandRepository {
    fun executeCommand(args: List<String>): CommandResult
    fun getCurrentBranch(): String
    fun fetchOrigin()
}