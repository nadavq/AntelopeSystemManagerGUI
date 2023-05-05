package ui

import com.module.*
import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.io.File
import java.lang.Exception


class AntelopeSystemManagerUI : Application() {

    private val commandHandler = CommandHandler()

    @FXML
    private lateinit var addProperties: Button

    @FXML
    private lateinit var propertiesGrid: GridPane

    @FXML
    private lateinit var isHotfix: CheckBox

    @FXML
    private lateinit var addToLastMigration: CheckBox

    @FXML
    private lateinit var addNewProp: Button

    @FXML
    private lateinit var errorLabel: Label

    @FXML
    private lateinit var applyClassToAllProps: Button

//    @FXML
//    private lateinit var removeFirstRowBtn: Button

//    @FXML
//    private lateinit var firstClassNameTextField: TextField

    private val numOfItemsInRow = 4

    private val firstRemoveRowBtnIndex = numOfItemsInRow * 2 - 1

    private val positionOfClassNameTFInEachRow = 2

    private val allClassesNames = Utils.getAllProjectClassesNames(File(StaticDataManager.SIGNAL_CRM_PATH), mutableListOf())

    override fun start(primaryStage: Stage?) {
        val loader = FXMLLoader(javaClass.getResource("/main_layout_new.fxml"))
        val root = loader.load() as Parent
        loader.getController<AntelopeSystemManagerUI>()

        primaryStage?.title = "Antelope System Manager"
        primaryStage?.scene = Scene(root)
        primaryStage?.show()
    }

    fun initialize() {
        addProperties.setOnAction { addProperties() }
        addNewProp.setOnAction { addNewRowForProp() }

        val btn = createNewRemoveRowBtn()
        btn.isDisable = true

        propertiesGrid.addRow(1, TextField(), createNewTypeNameTextField(), createNewClassNameTextField() , btn)

        if(StaticDataManager.SIGNAL_CRM_PATH.isEmpty()) {
            errorLabel.isVisible = true
            errorLabel.text = "Your SignalsCRM path is not initialized. Please click on File -> Paths"
        }
        else if(StaticDataManager.SIGNAL_SERVER_PATH.isEmpty()) {
            errorLabel.isVisible = true
            errorLabel.text = "Your SignalsServer path is not initialized. Please click on File -> Paths"
        }

        applyClassToAllProps.setOnAction { applyClassToAllProps() }
    }

    // Public Methods //

    // Private Methods //

    private fun applyClassToAllProps() {
        val gridChildren = propertiesGrid.children
        val positionOfFirstClassNameTF =positionOfClassNameTFInEachRow + numOfItemsInRow
        for(i in positionOfFirstClassNameTF until gridChildren.size step numOfItemsInRow) {
            (gridChildren[i] as TextField).text = (gridChildren[positionOfFirstClassNameTF] as TextField).text
        }
    }

    private fun addNewRowForProp() {
        val currentNewRowIndex = propertiesGrid.children.size / numOfItemsInRow
        val btn = createNewRemoveRowBtn()

        propertiesGrid.addRow(currentNewRowIndex, TextField(), createNewClassNameTextField(), createNewTypeNameTextField(), btn)
//        propertiesGrid.addRow(currentNewRowIndex, TextField(), TextField(), TextField(), btn)
//        removeFirstRowBtn.isDisable = false
        propertiesGrid.children[firstRemoveRowBtnIndex].isDisable = false
    }

    private fun createNewRemoveRowBtn(): Node {
        val btn = Button("-")
        val dropShadow = DropShadow()
        dropShadow.offsetX = 4.0
        dropShadow.offsetY = 4.0
        dropShadow.color = Color.GRAY
        btn.effect = dropShadow

        btn.style = "-fx-background-color:#19A7CE;"
        btn.textFill = Color.WHITE
        btn.setOnAction { removeRowFromGridPane(btn) }

        return btn
    }

    private fun createNewClassNameTextField() : AutoCompleteTextField {
        val textField = AutoCompleteTextField()
        textField.entries.addAll(this.allClassesNames)

        return textField
    }

    private fun createNewTypeNameTextField() : AutoCompleteTextField {
        val typeList = listOf("Integer", "String", "Long", "Double", "Date")
        val textField = AutoCompleteTextField()
        textField.entries.addAll(typeList)

        return textField
    }

    private fun removeRowFromGridPane(btn: Button) {
        val numRows = propertiesGrid.children.size / numOfItemsInRow // current number of rows in the grid pane
        val rowIndex = GridPane.getRowIndex(btn)
        propertiesGrid.children.removeIf { node -> GridPane.getRowIndex(node) == rowIndex } // remove nodes in the row
        for (i in rowIndex + 1 until numRows) {
            // update the row index for nodes in subsequent rows
            for (node in propertiesGrid.children) {
                if (GridPane.getRowIndex(node) == i) {
                    GridPane.setRowIndex(node, i - 1)
                }
            }
        }

        if (propertiesGrid.children.size / numOfItemsInRow == 2) {
            propertiesGrid.children[firstRemoveRowBtnIndex].isDisable = true
        }
    }

    private fun addProperties() {

        val resourcesPath = StaticDataManager.getPaths()

        if(resourcesPath.signalsCrmPath.isEmpty()) {
            setErrorLabel("CRM")
        }
        else if(resourcesPath.signalsServerPath.isEmpty()) {
            setErrorLabel("FE")
        }
        else if(resourcesPath.devMigrationPath.isEmpty()) {
            setErrorLabel("DEV")
        }
        else if(resourcesPath.prodMigrationPath.isEmpty()) {
            setErrorLabel("PROD")
        }
        else {

            try {
                val propertiesToAdd = mutableListOf<PropertyToAdd>()
                val nodes = propertiesGrid.children
                for (i in numOfItemsInRow until nodes.size step numOfItemsInRow) {
                    propertiesToAdd.add(
                        PropertyToAdd(
                            (nodes[i] as TextField).text,
                            ((nodes[i + 1] as TextField).text),
                            ((nodes[i + 2] as TextField).text)
                        )
                    )
                }

                if (propertiesToAdd.isNotEmpty()) {
                    commandHandler.handleAddProperties(propertiesToAdd, isHotfix.isPressed, addToLastMigration.isPressed)
                }
            } catch (e: Exception) {
                e.message?.let { setErrorLabel(it) }
            }
        }
    }

    fun initPaths() {
        InitPathsUI().start(Stage())
    }

    private fun setErrorLabel(error: String) {
        errorLabel.isVisible = true
        errorLabel.text = error
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(AntelopeSystemManagerUI::class.java, *args)
        }
    }
}
