package ui

import com.module.StaticDataManager
import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.*
import javafx.scene.control.*
import javafx.scene.paint.Paint
import javafx.stage.Stage

class InitPathsUI() : Application() {

    @FXML
    private lateinit var signalsCRMPath: TextField

    @FXML
    private lateinit var signalsServerPath: TextField

    @FXML
    private lateinit var devMigrationPath: TextField

    @FXML
    private lateinit var prodMigrationPath: TextField

    @FXML
    private lateinit var submit: Button

    @FXML
    private lateinit var errorLabel: Label

    override fun start(primaryStage: Stage?) {
        val loader = FXMLLoader(javaClass.getResource("/settings.fxml"))
        val root = loader.load() as Parent
        loader.getController<AntelopeSystemManagerUI>()

        primaryStage?.title = "Antelope System Manager Settings"
        primaryStage?.scene = Scene(root)
        primaryStage?.show()
    }

    fun initialize() {
        submit.setOnAction { setPaths() }
        signalsCRMPath.text = StaticDataManager.SIGNAL_CRM_PATH
        signalsServerPath.text = StaticDataManager.SIGNAL_SERVER_PATH
        devMigrationPath.text = StaticDataManager.DEV_MIGRATIONS_PATH
        prodMigrationPath.text = StaticDataManager.PROD_MIGRATIONS_PATH
    }

    private fun setPaths() {
        try {
            StaticDataManager.setPaths(
                signalsCRMPath.text.trim(),
                signalsServerPath.text.trim(),
                devMigrationPath.text.trim(),
                prodMigrationPath.text.trim()
            )

            submit.style = "-fx-background-color:green;"
            errorLabel.isVisible = true
            errorLabel.text = "Paths Updated Successfully"
            errorLabel.textFill = Paint.valueOf("green")
        } catch (e: Exception) {
            errorLabel.isVisible = true
            errorLabel.text = e.message
        }
    }
}
