package com.module

import jdk.nashorn.internal.parser.JSONParser
import java.io.File
import java.lang.Exception

class Main {


    companion object {

        private val commandHandler = CommandHandler()

        @JvmStatic
        fun main(args: Array<String>) {
            startProgram()
        }

        private fun startProgram() {
            greet()
            initFilesPaths()
            var input: String
            while(true){
                try {
                    println("Please enter your antelope command!")
                    input = readLine()!!
                    handleInput(input)
                }catch (e : Exception) {
                    println(e.message)
                }
            }
        }

        private fun greet(){
            println("Hello All! We are antelope - the best CRM in the worlddddd!")
        }

        private fun help() {
            println("Here are all the possible commands:")
            StaticDataManager.COMMANDS.forEach { item -> println(item) }
        }

        private fun handleInput(input: String) {
            try {
                when (input) {
                    "add prop" -> commandHandler.handleAddProperty()
                    "a p" -> commandHandler.handleAddProperty()
                    "help" -> help()
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }

        private fun initFilesPaths() {
            var crmFilePath: String
            var feFilePath : String
            val devMigrationPath : String
            val prodMigrationPath : String

            if(StaticDataManager.SIGNAL_CRM_PATH.equals("") || StaticDataManager.SIGNAL_SERVER_PATH.equals("")) {
                println("Please initialize your SignalsCRM path (this will only be required once)")
                crmFilePath = readLine()!!
                crmFilePath += "/src/main/java/com/mycompany/signalcrm"
                println("Please initialize your SignalsServer path (this will only be required once)")
                feFilePath = readLine()!!
                devMigrationPath = "$feFilePath/src/main/resources/db/migrations/dev"
                prodMigrationPath = "$feFilePath/src/main/resources/db/migrations/prod/all"

                feFilePath += "/src/main/java/com/mycompany/signalsserver"

                try {
                    Utils.appendTextToFile(StaticDataManager.thisFilePath ,"SIGNAL_CRM_PATH = \"", crmFilePath)
                    println("SignalCRM path updated successfully")
                    Utils.appendTextToFile(StaticDataManager.thisFilePath ,"SIGNAL_SERVER_PATH = \"", crmFilePath)
                    println("SignalServer path updated successfully")
                    Utils.appendTextToFile(StaticDataManager.thisFilePath ,"DEV_MIGRATIONS_PATH = \"", devMigrationPath)
                    println("SignalServer develop migration path updated successfully")
                    Utils.appendTextToFile(StaticDataManager.thisFilePath ,"PROD_MIGRATIONS_PATH = \"", prodMigrationPath)
                    println("SignalServer production migration updated successfully")

                    StaticDataManager.SIGNAL_CRM_PATH = crmFilePath
                    StaticDataManager.SIGNAL_SERVER_PATH = feFilePath
                    StaticDataManager.DEV_MIGRATIONS_PATH = devMigrationPath
                    StaticDataManager.PROD_MIGRATIONS_PATH = prodMigrationPath
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            val jsonString = File("path/to/json/file.json").readText()
        }
    }
}
