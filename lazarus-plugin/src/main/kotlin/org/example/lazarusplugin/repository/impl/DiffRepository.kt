package org.example.lazarusplugin.repository.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.models.git.FileChange
import org.example.lazarusplugin.repository.api.IDiffRepository

@Service(Service.Level.PROJECT)
class DiffRepository(private val project: Project) : IDiffRepository {
    private val commandRepo = project.service<CommandRepository>()
    private val log = Logger.getInstance(DiffRepository::class.java)

    override fun getDiff(fromRef: String, toRef: String, filePath: String?): String {
        val args = mutableListOf("diff", "-U0", "$fromRef..$toRef")
        filePath?.let { args.add(it) }

        val result = commandRepo.executeCommand(args)
        return if (result.exitCode == 0) result.stdout else ""
    }

    override fun getNameStatus(fromRef: String, toRef: String): List<FileChange> {
        val result = commandRepo.executeCommand(
            listOf("diff", "--name-status", fromRef, toRef)
        )

        return if (result.exitCode == 0 && result.stdout.isNotBlank()) {
            parseGitDiffOutput(result.stdout)
        } else {
            emptyList()
        }
    }

    override fun getStatus(): List<FileChange> {
        val result = commandRepo.executeCommand(listOf("status", "--porcelain"))

        return if (result.exitCode == 0 && result.stdout.isNotBlank()) {
            parseGitStatusOutput(result.stdout)
        } else {
            emptyList()
        }
    }

    private fun parseGitDiffOutput(output: String): List<FileChange> =
        output.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split("\t", limit = 2)
                if (parts.size == 2) FileChange(parts[0], parts[1]) else null
            }.toList()

    private fun parseGitStatusOutput(output: String): List<FileChange> =
        output.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                if (line.length >= 3) {
                    FileChange(
                        status = line.substring(0, 2).trim().ifEmpty { "M" },
                        path = line.substring(3)
                    )
                } else null
            }.toList()
}