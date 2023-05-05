package com.module

import java.io.File
import java.lang.Exception

class Utils {

    companion object {

        fun findFileFromRootDir(file: File, fileName: String) : File? {
            var foundFile : File?
            if(file.isDirectory) {
                for (innerFile in file.listFiles()) {
                    foundFile = findFileFromRootDir(innerFile, fileName)
                    if(foundFile != null) {
                        return foundFile
                    }
                }
            }
            else {
                if((file.name.toLowerCase()).equals("${fileName.toLowerCase()}.kt") || (file.name.toLowerCase()).equals("${fileName.toLowerCase()}.java") ){
                    println(file.path)
                    return file
                }
            }
            return null
        }

        fun appendTextToFile(path: String, afterThisText: String, newText: String) {
            val file = File(path)
            val fileContent = file.readText()
            val position = fileContent.indexOf(afterThisText)

            if(position == -1) {
                throw Exception("text $afterThisText not found in file ${file.name}")
            }

            val updatedText = fileContent.substring(0, position + afterThisText.length) + newText + fileContent.substring(position + afterThisText.length)
            file.writeText(updatedText)
        }

        fun appendTextBeforeTextToFile(path: String, beforeThisText: String, newText: String) {
                val file = File(path)
                val fileContent = file.readText()
                val position = fileContent.indexOf(beforeThisText)

                if (position == -1) {
                    throw Exception("text $beforeThisText not found in file ${file.name}")
                }

                val updatedText = fileContent.substring(0, position) + newText + fileContent.substring(position)
                file.writeText(updatedText)
        }

        fun getAllProjectClassesNames(file: File, classList: MutableList<String>) : List<String> {
            val crmDirectory = File(StaticDataManager.SIGNAL_CRM_PATH)

            if(crmDirectory.isDirectory && file.listFiles() != null) {
                for (innerFile in file.listFiles()) {
                    getAllProjectClassesNames(innerFile, classList)
                }
            }
            else if(file.name.contains(".java") || file.name.contains(".kt")){
                classList.add(file.name.replace(".kt", "").replace(".java", ""))
            }
            return classList
        }
    }
}
