package com.congxiaoyao.autoclick

import java.io.File

object Cons {

    val appDir: File
    val tempDir = File(System.getProperty("java.io.tmpdir"), "AutoClick")
    val newestAppFile = File(tempDir, "newest_app.jar")
    val installerFile = File(Cons.tempDir, "AutoClickInstaller.jar")

    init {
        val isWin = System.getProperty("os.name").contains("win")
        appDir = if (isWin) File("C:\\Program Files\\AutoClick") else tempDir

        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        println("tempDir = ${tempDir.absoluteFile}")
        println("appDir = ${appDir.absoluteFile}")
    }
}