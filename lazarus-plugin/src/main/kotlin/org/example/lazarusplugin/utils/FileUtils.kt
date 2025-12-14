package org.example.lazarusplugin.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Paths

object FileUtils {
    fun getFileContent(project: Project, filePath: String): String {
        val projectBasePath = project.basePath ?: return ""
        val absolutePath = Paths.get(projectBasePath, filePath).toString()
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath)

        return virtualFile?.let {
            String(it.contentsToByteArray())
        } ?: ""
    }
}
