package ui

import java.awt.GridLayout
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class MyFrame : JFrame() {
    private val addButton: JButton = JButton("Add Row")

    private var numOfRows = 1

    init {
        // Set the window title
        title = "AntelopeSystemManager"

        // Create a panel to hold the labels
        // Create a panel to hold the labels
        val panel = JPanel()

        // Use a GridLayout with one row and three columns
        panel.layout = GridLayout(1, 3)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10); // Add 10 pixels of padding on all sides

        // Create three labels
        val nameLabel = JLabel("Name")
        val typeLabel = JLabel("Type")
        val classLabel = JLabel("Class To Add To")
        addComponent(panel, nameLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE)
        addComponent(panel, typeLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE)
        addComponent(panel, classLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE)

        // Add the "Add Row" button
        addComponent(panel, addButton, 0, 1, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE)


        // Add the panel to the frame
        add(panel)

        // Set the size and visibility of the frame
        setSize(400, 100)
        isVisible = true

        addButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                // Add a new row of text fields when the button is clicked
                val nameField = JTextField(10)
                val typeField = JTextField(10)
                val classField = JTextField(10)
                addComponent(panel, nameField, 0, panel.componentCount, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true)
                addComponent(panel, typeField, 1, panel.componentCount - 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE)
                addComponent(panel, classField, 2, panel.componentCount - 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE)

                // Repaint the panel to update the UI
                panel.revalidate()
                panel.repaint()
            }
        })
    }

    private fun addComponent(
        container: Container,
        component: Component,
        gridx: Int,
        gridy: Int,
        gridwidth: Int,
        gridheight: Int,
        anchor: Int,
        fill: Int,
        addRow: Boolean = false
    ) {
        val constraints = GridBagConstraints().apply {
            this.gridx = gridx
            this.gridy = gridy
            this.gridwidth = gridwidth
            this.gridheight = gridheight
            this.anchor = anchor
            this.fill = fill
            this.insets = Insets(5, 5, 5, 5)
        }
        if(addRow) {
            numOfRows++
            container.layout = GridLayout(numOfRows, 3)
        }
        container.add(component, constraints)
    }
}
