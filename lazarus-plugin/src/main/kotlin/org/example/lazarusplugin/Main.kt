package org.example.lazarusplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.platform.backend.observation.Observation
import kotlinx.coroutines.runBlocking
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.ProjectActivity

class Main : ProjectActivity {
    override suspend fun execute(project: Project) {


        runBlocking {
            Observation.awaitConfiguration(project)
            project.waitForSmartMode()
        }

    }
}