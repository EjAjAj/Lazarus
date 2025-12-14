package org.example.lazarusplugin.git.service

import org.example.lazarusplugin.models.git.CommandResult

interface ICommandRepository {
    fun executeCommand(args: List<String>): CommandResult
    fun getCurrentBranch(): String
    fun fetchOrigin()
}