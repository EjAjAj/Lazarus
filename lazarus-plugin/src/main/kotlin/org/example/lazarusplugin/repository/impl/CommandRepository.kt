package org.example.lazarusplugin.repository.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.git.service.ICommandRepository
import org.example.lazarusplugin.models.git.CommandResult
import java.io.File
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class CommandRepository(private val project: Project) : ICommandRepository {
    private val repoPath: String get() = project.basePath ?: throw IllegalStateException("Project base path not found")
    private val log = Logger.getInstance(CommandRepository::class.java)

    override fun executeCommand(args: List<String>): CommandResult {
        val command = listOf("git") + args
        val processBuilder = ProcessBuilder(command)
            .directory(File(repoPath))
            .redirectErrorStream(false)

        System.getenv("SSH_AUTH_SOCK")?.let {
            processBuilder.environment()["SSH_AUTH_SOCK"] = it
        }

        return try {
            val process = processBuilder.start()
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()

            val completed = process.waitFor(30, TimeUnit.SECONDS)
            val exitCode = if (completed) process.exitValue() else -1

            CommandResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            log.error("Git command failed", e)
            CommandResult(-1, "", e.message ?: "Unknown error")
        }
    }

    override fun getCurrentBranch(): String {
        val result = executeCommand(listOf("rev-parse", "--abbrev-ref", "HEAD"))
        return if (result.exitCode == 0) {
            result.stdout.trim()
        } else {
            log.warn("Could not determine branch, defaulting to main")
            "main"
        }
    }

    override fun fetchOrigin() {
        executeCommand(listOf("fetch", "origin"))
    }
}
