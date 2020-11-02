package com.congxiaoyao.publish

import com.congxiaoyao.autoclick.versionCode
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import java.io.ByteArrayInputStream

fun main() {
    val session = JSch().getSession("ftpuser", "120.48.16.191")
    session.setPassword("ftp336996")
    session.setConfig("StrictHostKeyChecking", "NO")
    session.connect()
    val channel = session.openChannel("sftp") as ChannelSftp
    channel.connect()

    channel.put(
        "/Users/duhongliu/APSForMFW/AutoClick/build/libs/AutoClick.jar",
        "AutoClick/AutoClick.jar"
    )

    channel.put(
        ByteArrayInputStream(versionCode.toString().toByteArray()),
        "AutoClick/newest_version"
    )

    channel.disconnect()
    session.disconnect()
}