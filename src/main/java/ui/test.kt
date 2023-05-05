package ui

import javax.swing.*

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        val frame = MyFrame()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(400, 300)
        frame.isVisible = true
    }
}
