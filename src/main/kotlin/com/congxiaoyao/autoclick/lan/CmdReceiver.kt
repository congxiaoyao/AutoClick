package com.congxiaoyao.autoclick.lan

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket


class CmdReceiver {

    fun start() {
        val buf = ByteArray(1024)
        try {
            val socket = DatagramSocket(BROADCAST_PORT)
            while (true) {
                val packet = DatagramPacket(buf, buf.size)
                socket.receive(packet)
                println("receive packet!")
                val cmd = String(packet.data, 0, packet.length)
                handleCommand(cmd)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleCommand(cmd: String) {
        if (cmd != "shutdown") return
        try {
            println("exec $cmd")
            Runtime.getRuntime().exec("shutdown -s -t 5")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}