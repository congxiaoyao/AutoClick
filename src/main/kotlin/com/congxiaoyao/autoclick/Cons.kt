package com.congxiaoyao.autoclick

import java.io.File

object Cons {

    val appDir: File
    val externalLibsDir: File
    val userDataDir:File

    val tempDir = File(System.getProperty("java.io.tmpdir"), "AutoClick")
    val newestAppFile = File(tempDir, "newest_app.jar")
    val installerFile = File(tempDir, "AutoClickInstaller.jar")
    val userDefaultTimeFile:File

    const val URL_SERVER = "http://120.48.16.191:7000/AutoClick"
    const val URL_REMOTE_LIBS = "${URL_SERVER}/libs/"
    const val URL_CHECK_NEW = "${URL_SERVER}/newest_version"
    const val URL_DOWNLOAD_APP = "${URL_SERVER}/app"

    init {
        appDir = if (AppLauncher.isWin()) File("D:\\AutoClick") else tempDir
        externalLibsDir = File(appDir, "libs")
        userDataDir = File(appDir, "data")
        userDefaultTimeFile = File(userDataDir, "defaultTime")

        ensureDir(appDir)
        ensureDir(tempDir)
        ensureDir(userDataDir)

        println("tempDir = ${tempDir.absoluteFile}")
        println("appDir = ${appDir.absoluteFile}")
    }

    private fun ensureDir(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }
}