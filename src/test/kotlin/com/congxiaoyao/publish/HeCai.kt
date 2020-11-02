package com.congxiaoyao.publish

import java.io.File

fun main() {
    File("/Users/duhongliu/APSForMFW/AutoClick/build/libs/AutoClick.jar")
        .copyTo(File("/Users/duhongliu/Desktop/定时点击.jar"), true)
}