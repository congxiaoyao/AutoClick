package com.congxiaoyao.autoclick

import java.awt.Font
import javax.swing.JComponent

var JComponent.fontSize get() = font.size
set(value) {
    font = Font(font.name, font.style, value)
}