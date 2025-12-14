package org.example.lazarusplugin.git.service

import CommandRepository
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.example.lazarusplugin.git.models.FileChange
import org.example.lazarusplugin.git.repository.DiffRepository

@Service(Service.Level.PROJECT)
class RobustGitDiffService(private val project: Project) : IDiffService {
    private val commandRepo = project.service<CommandRepository>()
    private val diffRepo = project.service<DiffRepository>()

    private val log = Logger.getInstance(RobustGitDiffService::class.java)

    override fun getChangedFiles(): List<FileChange> {
        log.info("Starting robust Git diff analysis")

        val strategies = listOf(
            ::tryOriginVsHead,
            ::tryHeadVsOrigin,
            ::tryThreeWayDiff,
            ::tryLastCommit,
            ::tryWorkingDirectory
        )

        for ((index, strategy) in strategies.withIndex()) {
            try {
                log.info("Attempting strategy ${index + 1}/${strategies.size}")
                val result = strategy()

                if (result.isNotEmpty()) {
                    log.info("Strategy ${index + 1} succeeded with ${result.size} files")
                    return result
                }
            } catch (e: Exception) {
                log.warn("Strategy ${index + 1} failed: ${e.message}", e)
            }
        }

        log.info("No changes detected")
        return emptyList()
    }

    override fun getChangedFilesWithDiffs(): Map<String, String> {
        val changedFiles = getChangedFiles()

        if (changedFiles.isEmpty()) {
            log.info("No changed files to diff")
            return emptyMap()
        }

        val currentBranch = commandRepo.getCurrentBranch()
        val basePath = project.basePath ?: ""
        val diffs = mutableMapOf<String, String>()

        for (file in changedFiles) {
            val diff = diffRepo.getDiff("HEAD", "origin/$currentBranch", file.path)

            if (diff.isNotBlank()) {
                val absolutePath = java.io.File(basePath, file.path).absolutePath
                diffs[absolutePath] = diff
            } else {
                log.debug("No diff for ${file.path}")
            }
        }

        return diffs
    }

    private fun tryOriginVsHead(): List<FileChange> {
        commandRepo.fetchOrigin()
        val currentBranch = commandRepo.getCurrentBranch()
        log.info("Current branch: $currentBranch")
        return diffRepo.getNameStatus("origin/$currentBranch", "HEAD")
    }

    private fun tryHeadVsOrigin(): List<FileChange> {
        val currentBranch = commandRepo.getCurrentBranch()
        return diffRepo.getNameStatus("HEAD", "origin/$currentBranch")
    }

    private fun tryThreeWayDiff(): List<FileChange> {
        val currentBranch = commandRepo.getCurrentBranch()
        return diffRepo.getNameStatus("origin/$currentBranch...HEAD", "")
    }

    private fun tryLastCommit(): List<FileChange> {
        return diffRepo.getNameStatus("HEAD~1", "HEAD")
    }

    private fun tryWorkingDirectory(): List<FileChange> {
        return diffRepo.getStatus()
    }
}