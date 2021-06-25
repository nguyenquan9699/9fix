package org.jetbrains.plugins.ninefix.services

import com.intellij.openapi.project.Project
import org.jetbrains.plugins.ninefix.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
