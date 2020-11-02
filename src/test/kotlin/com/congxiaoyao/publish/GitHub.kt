package com.congxiaoyao.publish

import com.congxiaoyao.autoclick.versionCode

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

/**
 * 工作流程：
 * 1.完成功能开发/bugfix工作
 * 2.修改版本号
 * 3.打jar包
 * 4.运行此文件
 * 5.打开github客户端，粘贴commit信息并提交
 */
fun main() {
    //copy jar file to github folder
    File("/Users/duhongliu/APSForMFW/AutoClick/build/libs/AutoClick.jar").inputStream()
        .copyTo(File("/Users/duhongliu/Documents/GitHub/MyJavaUtils/AutoClick.jar").outputStream())
    //copy version code to github folder
    File("/Users/duhongliu/Documents/GitHub/SoftwareUpdateCheck/newest_version")
        .writeText(versionCode.toString())
    //copy version code to clipboard
    Toolkit.getDefaultToolkit().systemClipboard
        .setContents(StringSelection("v$versionCode"), null)
}