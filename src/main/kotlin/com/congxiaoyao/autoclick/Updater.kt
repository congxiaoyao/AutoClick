package com.congxiaoyao.autoclick

import java.awt.Color
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.swing.JLabel
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class Updater(private val versionLabel: JLabel) {

    @Volatile
    internal var updatePrepared = false

    private val uiThread = UIThread()
    private val versionText = "v$versionName"
    private val checkUpdateText = Array(4) { "$versionText${buildString { repeat(it) { append('.') } }}" }
    private fun downloadingText(progress: Double) = "$versionText(正在下载新版本 ${(progress * 100).toInt()}%)"
    private val downloadingText = arrayOf(
        "$versionText(正在下载新版本)", "$versionText(正在下载新版本.)",
        "$versionText(正在下载新版本..)", "$versionText(正在下载新版本...)"
    )
    private val unZipInstallerText = arrayOf(
        "$versionText(正在安装)", "$versionText(正在安装.)",
        "$versionText(正在安装..)", "$versionText(正在安装...)"
    )

    fun checkUpdate() {
        thread {
            try {
                checkUpdateBackground()
            } catch (e: Exception) {
                versionLabel.foreground = Color.RED
                versionLabel.text = "v$versionName"
            }
        }
    }

    private fun checkUpdateBackground() {
        uiThread.post(Runnable {
            var index = 0
            while (true) {
                versionLabel.text = checkUpdateText[index]
                index = (index + 1) % checkUpdateText.size
                Thread.sleep(500)
            }
        })

        val newestCode = fetchNewestVersion()
        uiThread.cancelRunningTask()

        if (newestCode <= versionCode) {
            versionLabel.text = "$versionText($versionCode)"
            destroy()
            return
        }

        //1.下载最新jar包
        downloadApp()
        updatePrepared = true
        //2.解压安装器
        unZipInstaller()
        if (!remainLabel.isCounting) {
            //3.调起安装器
            runInstaller()
            //4.关闭自己，等待被调起
            exitProcess(0)
        }
    }

    private fun runInstaller() {
        val targetDirPath = getAppFileParentPath()
        val sourcePath = Cons.newestAppFile.absolutePath
        val arg = "$targetDirPath;;;;;$sourcePath"

        val jarFile = File(Cons.installerFile.absolutePath)
        val cmd = "java -jar ${jarFile.name} $arg"
        try {
            Runtime.getRuntime().exec(cmd, null, jarFile.parentFile)
        } catch (e: IOException) {
        }
    }

    private fun unZipInstaller() {
        uiThread.post(Runnable {
            var index = 0
            while (true) {
                versionLabel.text = unZipInstallerText[index]
                index = (index + 1) % unZipInstallerText.size
                Thread.sleep(500)
            }
        })
        PathTest::class.java.getResourceAsStream("/AutoClickInstaller.jar")?.copyTo(Cons.installerFile.outputStream())
        uiThread.cancelRunningTask()
    }

    fun getAppFileParentPath(): String {
        var path = ""
        try {
            //jar 中没有目录的概念
            val location: URL = PathTest::class.java.protectionDomain.codeSource?.location ?: return path
            val file = File(location.getPath()) //构建指向当前URL的文件描述符
            path = if (file.isDirectory) { //如果是目录,指向的是包所在路径，而不是文件所在路径
                file.absolutePath //直接返回绝对路径
            } else { //如果是文件,这个文件指定的是jar所在的路径(注意如果是作为依赖包，这个路径是jvm启动加载的jar文件名)
                file.parent //返回jar所在的父路径
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return path
    }

    private fun downloadApp() {
        uiThread.post(Runnable {
            var index = 0
            while (true) {
                versionLabel.text = downloadingText[index]
                index = (index + 1) % downloadingText.size
                Thread.sleep(500)
            }
        })
        URL(Cons.URL_DOWNLOAD_APP).openConnection().apply {
            connect()
            getInputStream().use {
                it.copyTo(Cons.newestAppFile.outputStream())
            }
        }
        uiThread.cancelRunningTask()
    }

    private fun fetchNewestVersion(): Int {
        return try {
            URL(Cons.URL_CHECK_NEW).openConnection().run {
                connect()
                getInputStream().use {
                    it.reader().readText().trim().toInt()
                }
            }
        } catch (e: Exception) {
            -1
        }
    }

    private fun destroy() {
        uiThread.shutDown()
    }

    private class UIThread {
        private val executor = Executors.newSingleThreadExecutor()
        private var lastTask: Future<*>? = null
        fun post(runnable: Runnable) {
            cancelRunningTask()
            lastTask = executor.submit(runnable)
        }

        fun cancelRunningTask() {
            if (lastTask?.isCancelled == false || lastTask?.isDone == false) {
                lastTask?.cancel(true)
            }
        }

        fun shutDown() {
            executor.shutdown()
        }
    }
}