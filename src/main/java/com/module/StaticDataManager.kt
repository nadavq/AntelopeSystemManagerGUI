package com.module

import com.google.gson.Gson
import ui.AntelopeSystemManagerUI
import java.io.File

class StaticDataManager {

    companion object {

        val thisFilePath = "src/main/java/com/module/StaticDataManager.kt"

        var SIGNAL_CRM_PATH : String

        var SIGNAL_SERVER_PATH : String

        var DEV_MIGRATIONS_PATH : String

        var PROD_MIGRATIONS_PATH : String

        val COMMANDS = arrayOf("add prop")

        val ADD_PROP_POSITION = "// ------------------------ Public methods"

        val ADD_PROP_POSITION_RO = "public"

        val gson = Gson()

        init {

            var resourcesPath : ResourcesPath

            val url = javaClass.getResource("")
            val jsonFile = File("${url.path}/paths.json")
            if(jsonFile.createNewFile()) {
                resourcesPath = ResourcesPath("", "", "", "")
                jsonFile.writeText(gson.toJson(resourcesPath))
            }
            else {
                val jsonString = jsonFile.readText()
                resourcesPath = gson.fromJson(jsonString, ResourcesPath::class.java)
            }

            this.SIGNAL_CRM_PATH = resourcesPath.signalsCrmPath
            this.SIGNAL_SERVER_PATH = resourcesPath.signalsServerPath
            this.DEV_MIGRATIONS_PATH = resourcesPath.devMigrationPath
            this.PROD_MIGRATIONS_PATH = resourcesPath.prodMigrationPath
        }

        fun setPaths(crmPath: String, fePath: String, devMigrationPath: String, prodMigrationPath: String) {
            val resourcesPath = ResourcesPath(crmPath, fePath, devMigrationPath, prodMigrationPath)
            val url = javaClass.getResource("")
            val jsonFile = File("${url.path}/paths.json")
            jsonFile.writeText(gson.toJson(resourcesPath))

            this.SIGNAL_CRM_PATH = resourcesPath.signalsCrmPath
            this.SIGNAL_SERVER_PATH = resourcesPath.signalsServerPath
            this.DEV_MIGRATIONS_PATH = resourcesPath.devMigrationPath
            this.PROD_MIGRATIONS_PATH = resourcesPath.prodMigrationPath
        }

        fun getPaths() : ResourcesPath {
            return ResourcesPath(SIGNAL_CRM_PATH, SIGNAL_SERVER_PATH, DEV_MIGRATIONS_PATH, PROD_MIGRATIONS_PATH)
        }
    }

}
