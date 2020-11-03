package com.congxiaoyao.autoclick.lan

import java.io.IOException
import java.net.*
import java.util.*


internal const val BROADCAST_PORT = 44866

object CmdSender {

    fun sendCommand(cmd: String) {
        val ds = DatagramSocket()
        try {
            val adds = InetAddress.getByName("255.255.255.255")
            val data = cmd.toByteArray()
            val dp = DatagramPacket(data, data.size, adds, BROADCAST_PORT)
            ds.send(dp)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            ds.close()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            val cmd = scanner.nextLine()
            sendCommand(cmd)
        }
    }
}