package com.congxiaoyao.autoclick

import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

fun main() {

    val jarFile = JarFile(File("src/main/resources/AutoClick.jar"))
    if (jarFile.stream()
            .anyMatch { jarEntry: JarEntry ->
                jarEntry.name.contains("com/congxiaoyao/ClickThread")
            }
    ) {
        println("true!!")
    }

//    jarFile.entries().asSequence().forEach {
//        println(it.name)
//    }
}