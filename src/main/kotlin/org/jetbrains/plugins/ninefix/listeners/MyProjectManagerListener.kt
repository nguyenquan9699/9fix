package org.jetbrains.plugins.ninefix.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import org.apache.commons.io.IOUtils
import org.jetbrains.plugins.ninefix.Config
import org.jetbrains.plugins.ninefix.services.MyProjectService
import java.io.File
import java.io.InputStream
import java.net.URL


internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        projectInstance = project
        Config.PATH = projectInstance!!.basePath.toString() + "/Rules.xml"
        val rulesFile = File(Config.PATH)
        when {
            !rulesFile.exists() -> {
                val inputStream: InputStream = URL(Config.RULES_URL).openStream()
                IOUtils.copy(inputStream, rulesFile.outputStream())
            }
            else -> {

            }
        }

        project.getService(MyProjectService::class.java)

    }

    override fun projectClosing(project: Project) {
        projectInstance = null
        Config.PATH = ""
        super.projectClosing(project)
    }

    companion object {
        var projectInstance: Project? = null
    }
}
