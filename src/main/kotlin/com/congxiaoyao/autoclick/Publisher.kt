package com.congxiaoyao.autoclick

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File


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