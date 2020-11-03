package com.congxiaoyao.autoclick

import com.congxiaoyao.autoclick.lan.CmdReceiver
import com.congxiaoyao.autoclick.lan.CmdSender
import java.awt.*
import java.awt.event.*
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import kotlin.concurrent.thread


var sw = Toolkit.getDefaultToolkit().screenSize.width
var sh = Toolkit.getDefaultToolkit().screenSize.height

internal val window = JFrame("定时点击")
private val timeLabel = JLabel()
private val editTexts = arrayOfNulls<JTextArea>(4)
internal val remainLabel = RemainLabel()
private val versionLabel = JLabel("v$versionName").apply { foreground = Color.GRAY }

private val updater =Updater(versionLabel)

private val format = SimpleDateFormat("HH:mm:ss:SSS")

fun main() {
    initUI()
    initMenu()
    setUpClockWorker()
    initLan()
    updater.checkUpdate()
}

fun initLan() {
    thread {
        CmdReceiver().start()
    }
}

fun setUpClockWorker() {
    thread {
        while (true) {
            val time = format.format(Date(System.currentTimeMillis()))
            timeLabel.text = "当前时间：$time"
            if (remainLabel.isCounting) {
                val delta = (remainLabel.target - System.currentTimeMillis()) / 1000
                val min = delta / 60
                val sec = delta % 60
                remainLabel.text = "剩余时间：${min}分钟 ${sec}秒"
            }
            Thread.sleep(10)
        }
    }
}

private fun initUI() {
    val w = 800
    val h = 300
    window.setBounds((sw - w) / 2, (sh - h) / 2, w, h)
    window.layout = BorderLayout()

    addComponentsToWindow()

    window.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(event: WindowEvent) = windowClosed(event)
        override fun windowClosed(event: WindowEvent) {
            saveUserData()
        }
    })
    window.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    window.isVisible = true
}

fun initMenu() {
    window.jMenuBar = JMenuBar().apply {
        add(JMenu("菜单").apply {
            add(JMenuItem("全部关机").apply {
                addActionListener {
                    CmdSender.sendCommand("shutdown")
                }
            })
        })
    }
}

internal fun saveUserData() {
    Cons.userDefaultTimeFile.writer().buffered().use { writer ->
        editTexts.requireNoNulls().map { it.text }.forEach {
            writer.write(it)
            writer.newLine()
            writer.flush()
        }
    }
}

private fun loadUserData(): List<String> {
    return try {
        Cons.userDefaultTimeFile.readLines().take(4)
    } catch (e: Throwable) {
        e.printStackTrace()
        listOf("8", "45", "1", "0")
    }
}

private fun addComponentsToWindow() {
    window.add(JPanel().apply {
        timeLabel.fontSize = 16
        layout = FlowLayout(FlowLayout.CENTER)
        add(timeLabel)
    }, BorderLayout.NORTH)

    window.add(JPanel().apply {
        layout = FlowLayout(FlowLayout.CENTER, 20, 20)
        add(remainLabel.apply {
            horizontalAlignment = SwingConstants.CENTER
            preferredSize = Dimension(sw, 50)
            text = "请定时"
            fontSize = 40
            this.foreground = Color.RED
        })
        val text = arrayOf("时", "分", "秒", "毫秒")
        repeat(4) {
            add(JTextArea().apply {
                if (it == 3) {
                    this.preferredSize = Dimension(100, 46)
                } else {
                    this.preferredSize = Dimension(50, 46)
                }
                editTexts[it] = this
                fontSize = 36
            })
            add(JLabel(text[it]).apply {
                fontSize = 24
            })
        }
    }, BorderLayout.CENTER)

    val timeStr = loadUserData()
    repeat(editTexts.size) {
        editTexts[it]!!.text = timeStr[it]
    }

    window.add(JPanel().apply {
        val calendar = Calendar.getInstance()
        layout = BorderLayout()
        add(JButton().apply {
            text = "开始"
            fontSize = 16
            addActionListener {
                try {
                    checkUserInput()
                    repeat(editTexts.size) {
                        calendar.set(Calendar.HOUR_OF_DAY + it, editTexts[it]!!.text.toInt())
                    }
                    remainLabel.startTask(calendar.time.time)
                } catch (e: Exception) {
                    remainLabel.isCounting = false
                    remainLabel.text = "请定时"
                    JOptionPane.showMessageDialog(
                        window,
                        "时间设置非法",
                        "错误",
                        JOptionPane.WARNING_MESSAGE
                    )
                }
            }
        }, BorderLayout.EAST)
        add(JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT)
            add(JLabel().apply { preferredSize = Dimension(0, 1) })
            add(versionLabel, BorderLayout.WEST)
        }, BorderLayout.WEST)
    }, BorderLayout.SOUTH)

    versionLabel.addMouseListener(object : MouseAdapter() {
        var lastClickTime = -1L
        override fun mouseClicked(p0: MouseEvent?) {
            if (lastClickTime > 0) {
                val delta = System.currentTimeMillis() - lastClickTime
                if (delta < 350) {
                    lastClickTime = -1
                    updater.reInstallFromServer()
                }
            }
            lastClickTime = System.currentTimeMillis()
        }
    })
}

fun checkUserInput() {
    require(editTexts[0]!!.text.toInt() in 0 until 24)
    require(editTexts[1]!!.text.toInt() in 0 until 60)
    require(editTexts[2]!!.text.toInt() in 0 until 60)
    require(editTexts[3]!!.text.toInt() in 0 until 1000)
}

class RemainLabel : JLabel() {
    var target = 0L
    var clickThread: ClickThread? = null
    @Volatile
    var isCounting = false

    fun startTask(targetMills: Long) {
        this.target = targetMills
        val delay = targetMills - System.currentTimeMillis()
        if (delay < 0) {
            isCounting = false
            remainLabel.text = "请定时"
            JOptionPane.showMessageDialog(
                window,
                "时间设置非法",
                "错误",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }
        val w = 400
        val h = 200
        setBounds(
            window.x + (window.width - w) / 2,
            window.y + (window.height - h) / 2,
            w, h
        )
        if (!updater.updatePrepared) {
            isCounting = true
            clickThread?.interrupt()
            clickThread = ClickThread(targetMills)
            clickThread?.start()
        }
    }
}

class ClickThread(private val targetMills: Long) : Thread() {
    override fun run() {
        val robot = Robot()
        val delay = targetMills - System.currentTimeMillis()
        if (delay < 0) {
            return
        }
        if (isInterrupted) return

        try {
            sleep(delay)
        } catch (e: Exception) {
            return
        }
        if (remainLabel.isCounting) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            sleep(1)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        }

        remainLabel.isCounting = false
        remainLabel.text = "请定时"
    }
}