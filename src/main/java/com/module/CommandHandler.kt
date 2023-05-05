package com.module

import ui.PropertyToAdd
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CommandHandler {

    val crmDirectory = File(StaticDataManager.SIGNAL_CRM_PATH)
    val feDirectory = File(StaticDataManager.SIGNAL_SERVER_PATH)

    fun handleAddProperty() {
        println("Enter property name")
        var name = readLine()!!
        while (name.equals("")) {
            println("property name must not be empty. Please enter a valid name:")
            name = readLine()!!
        }

        println("Enter property type:")
        var type = readLine()!!
//        while (type == "" || ((type.toLowerCase() != "long") && type.toLowerCase() != "int") && type.toLowerCase() != "boolean" && type.toLowerCase() != "string" && type.toLowerCase() != "date" && type.toLowerCase() != "double")  {
//            println("Please enter a valid property type: (long/int/string/boolean)")
//            type = readLine()!!
//        }

        println("To what class should we add it to?")
        var clazz = readLine()!!
        while (clazz.equals("")) {
            println("property class must not be empty. Please enter a valid class:")
            clazz = readLine()!!
        }

        if(clazz.toLowerCase() == "user") {
            println("we saw you want to add a property to the class User, would you like to add it to class UserAdditionalInfoRel instead?")
            val addToAdditionalInfo = readLine()!! == "y"
            if(addToAdditionalInfo){
                clazz = "UserAdditionalInfoRel"
            }
        }

        val originalClazz = clazz
        val originalType = type.toLowerCase()
        val clazzFile = addPropertyToClass(type, name, clazz, StaticDataManager.ADD_PROP_POSITION, crmDirectory)
        addPropertyToClass(type, name, clazz, StaticDataManager.ADD_PROP_POSITION, feDirectory)
        if(clazz == "UserAdditionalInfoRel"){
            clazz = "User"
        }
        addPropertyToClass(type, name, "${clazz}RO", StaticDataManager.ADD_PROP_POSITION_RO, crmDirectory, true)
        addPropertyToClass(type, name, "${clazz}RO", StaticDataManager.ADD_PROP_POSITION_RO, feDirectory, true)
        if(originalClazz == "UserAdditionalInfoRel"){
            clazz = originalClazz
        }
        handleSpecialCase(clazz, name)
        if (originalType == "date"){
            type = "dateTime"
        }
        addPropertyToDBMigration(type, name, clazz, clazzFile)

        println("Is this a hotfix? (should we add it to db migration) y/n")
        val isHotfix = readLine()!! == "y"
        if(isHotfix) {
            addPropertyToDBMigration(type, name, clazz, clazzFile, false)
        }

        println("Property added successfully!")
    }

    // UI
    fun handleAddProperties(props: List<PropertyToAdd>, isHotfix: Boolean, addPropToExistingMigrationFile: Boolean) {
        for (prop in props) {
            handleAddPropertyInternal(prop.name, prop.type, prop.classToAdd, isHotfix, addPropToExistingMigrationFile)
        }
    }

    // UI
    private fun handleAddPropertyInternal(propName: String, typeName: String, clazzName: String, isHotfix: Boolean, addPropToExistingMigrationFile: Boolean) {
        var clazz = clazzName
        var type = typeName
        var name = propName

        val originalClazz = clazz
        val originalType = type.toLowerCase()

        if(clazz.toLowerCase() == "user") {
            clazz = "UserAdditionalInfoRel"
        }

        val clazzFile = addPropertyToClass(type, name, clazz, StaticDataManager.ADD_PROP_POSITION, crmDirectory)
        addPropertyToClass(type, name, clazz, StaticDataManager.ADD_PROP_POSITION, feDirectory)
        if(clazz == "UserAdditionalInfoRel"){
            clazz = "User"
        }
        addPropertyToClass(type, name, "${clazz}RO", StaticDataManager.ADD_PROP_POSITION_RO, crmDirectory, true)
        addPropertyToClass(type, name, "${clazz}RO", StaticDataManager.ADD_PROP_POSITION_RO, feDirectory, true)
        if(originalClazz == "UserAdditionalInfoRel"){
            clazz = originalClazz
        }
        handleSpecialCase(clazz, name)
        if (originalType == "date"){
            type = "dateTime"
        }
        addPropertyToDBMigration(type, name, clazz, clazzFile, addPropToExistingMigrationFile, true)

        if(isHotfix) {
            addPropertyToDBMigration(type, name, clazz, clazzFile, addPropToExistingMigrationFile, false)
        }
    }

    private fun handleSpecialCase(clazz: String, name: String) {
        when(clazz.toLowerCase()) {
            "UserAdditionalInfoRel".toLowerCase() -> addMappedFieldForUserAdditionalInfoRel(name)
            "User".toLowerCase() -> addMappedFieldForUserAdditionalInfoRel(name)
        }
    }

    private fun addMappedFieldForUserAdditionalInfoRel(name: String) {
        val textToAdd = "@MappedField(mapFrom = \"$name\")\n\t"
        Utils.appendTextBeforeTextToFile(Utils.findFileFromRootDir(crmDirectory, "User")!!.absolutePath, "private UserAdditionalInfoRel",textToAdd)
    }

    private fun addPropertyToClass(type: String, name: String, clazz: String, beforeThisText: String, rootDir: File, isRO: Boolean = false): File? {
        println("Searching for class $clazz...")
        val requestedFile = Utils.findFileFromRootDir(rootDir, clazz)
        if(requestedFile == null) {
            println("Class named $clazz not found")
            return null
        }
        val isKotlinFile = requestedFile.name.indexOf(".kt") != -1
        val fileContent = requestedFile.readText()

        val position: Int = if(isKotlinFile) {
            fileContent.lastIndexOf("= null")
        } else{
            if(isRO) fileContent.indexOf(beforeThisText, fileContent.indexOf(beforeThisText) + 1, ignoreCase = true) else fileContent.indexOf(beforeThisText)
        }

        val propTextToAdd = if(isKotlinFile) createPropText(position, fileContent, type, name, isKotlinFile, true, "= null") else createPropText(position,fileContent,type,name, isKotlinFile)
        requestedFile.writeText(propTextToAdd)
        if (!isKotlinFile) {
            addGetterAndSetter(requestedFile, type, name, isRO)
        }
        return requestedFile
    }

    private fun addGetterAndSetter(requestedFile: File, type: String, name: String, isRO: Boolean = false) {
        val getterAndSetterText = createGetterAndSetterText(type, name, isRO)
        try {
            Utils.appendTextBeforeTextToFile(requestedFile.absolutePath, "// ------------------------ Other Public Methods", getterAndSetterText)
        } catch (e: Exception) {
            try {
                Utils.appendTextBeforeTextToFile(requestedFile.absolutePath, "\t@Override\n" +
                        "\tpublic boolean equals(Object o) {", getterAndSetterText + "\n\n")
            } catch (e: java.lang.Exception) {
                println(e.message)
            }
            println(e.message)
        }
    }

    private fun createGetterAndSetterText(type: String, name: String, isRO: Boolean = false): String {
        val generalText = "\tpublic ${type.capitalize()} get${name.capitalize()}() {\n" +
                "\t\treturn $name;\n" +
                "\t}\n" +
                "\n" +
                "\tpublic void set${name.capitalize()}(${type.capitalize()} $name) {\n" +
                "\t\tthis.$name = $name;\n" +
                "\t}"

        return if(isRO) {
            generalText
        }
        else {
            "\n" +
                    "\t@Column(name = \"$name\")\n" + generalText
        }
    }

    private fun addPropertyToDBMigration(type: String, name: String, clazz: String, clazzFile: File?, isDev: Boolean = true) {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val formattedDateTime = currentDateTime.format(formatter)
        val filesArray = File(if(isDev) StaticDataManager.DEV_MIGRATIONS_PATH else StaticDataManager.PROD_MIGRATIONS_PATH).listFiles()

        val lastMigrationFile = filesArray?.maxByOrNull { it.lastModified() }
        println("add property to last migration? (y/n)")
        println("if not, a new one will be created for it")
        var addPropToExistingMigrationFile = readLine()!!
        while(addPropToExistingMigrationFile != "y" && addPropToExistingMigrationFile != "n") {
            println("Please enter a valid answer")
            addPropToExistingMigrationFile = readLine()!!
        }
        val addPropToExistingMigrationFileBool = addPropToExistingMigrationFile == "y"

        val file: File
        var newText = ""
        val tableName = findTableName(clazzFile)

        if(addPropToExistingMigrationFileBool) {
            file = lastMigrationFile?.listFiles()?.maxByOrNull { it.lastModified() }!!
            newText = file.readText() + "\n"
        }
        else {
            file = File("${lastMigrationFile?.absolutePath}/V${formattedDateTime}__AddColumnsTo${clazz}.sql")
        }
        val propNameConvertedToDbName = convertPropNameToDbName(name)
        val propTypeConvertedToDbType = convertPropTypeToDbType(type) ?: throw Exception("property type should be equal to one of the follows: long, int, string, boolean")
        file.writeText(newText +"CALL safe_add_column('$tableName', '$propNameConvertedToDbName', '$propTypeConvertedToDbType');")
    }

    // UI
    private fun addPropertyToDBMigration(type: String, name: String, clazz: String, clazzFile: File?, addPropToExistingMigrationFile: Boolean, isDev: Boolean) {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val formattedDateTime = currentDateTime.format(formatter)
        val filesArray = File(if(isDev) StaticDataManager.DEV_MIGRATIONS_PATH else StaticDataManager.PROD_MIGRATIONS_PATH).listFiles()

        val lastMigrationFile = filesArray?.maxByOrNull { it.lastModified() }

        val file: File
        var newText = ""
        val tableName = findTableName(clazzFile)

        if(addPropToExistingMigrationFile) {
            file = lastMigrationFile?.listFiles()?.maxByOrNull { it.lastModified() }!!
            newText = file.readText() + "\n"
        }
        else {
            file = File("${lastMigrationFile?.absolutePath}/V${formattedDateTime}__AddColumnsTo${clazz}.sql")
        }
        val propNameConvertedToDbName = convertPropNameToDbName(name)
        val propTypeConvertedToDbType = convertPropTypeToDbType(type) ?: throw Exception("property type should be equal to one of the follows: long, int, string, boolean")
        file.writeText(newText +"CALL safe_add_column('$tableName', '$propNameConvertedToDbName', '$propTypeConvertedToDbType');")
    }

    private fun convertPropTypeToDbType(type: String): String? {
        return when (type.toLowerCase().trim()) {
            "long" -> "bigint(20) default null"
            "int" -> "int(11) default null"
            "double" -> "double default null"
            "string" -> "varchar(255) default null"
            "boolean" -> "tinyint(1) default 0 not null"
            "bool" -> "tinyint(1) default 0 not null"
            "datetime" -> "datetime null"
            else -> null
        }
    }

    private fun convertPropNameToDbName(propName: String): String {
        val result = StringBuilder()
        for (i in propName.indices) {
            val c = propName[i]
            if (i > 0 && c.isUpperCase()) {
                result.append('_')
            }
            result.append(c.toLowerCase())
        }
        return result.toString()
    }

    private fun findTableName(clazzFile: File?): String? {
        val fileText = clazzFile?.readText()
        var positionOfStartTableName = fileText?.indexOf("@Table(name = \"")
        if(positionOfStartTableName == -1) {
            throw Exception("DB table name not found in ${clazzFile?.name}")
        }
        positionOfStartTableName = positionOfStartTableName?.plus("@Table(name = \"".length)
        val afterStartTableNameSubString = positionOfStartTableName?.let { fileText?.substring(it) }
        val positionOfEndTableName = afterStartTableNameSubString?.indexOf("\"")
        val tableName = positionOfStartTableName?.let { fileText?.substring(it, positionOfStartTableName + positionOfEndTableName!!) }
        println("table name found: $tableName")
        return tableName
    }

    private fun createPropText(position: Int, fileContent: String, type: String, name: String, isKotlinFile: Boolean, isAfterText: Boolean = false, afterThisText : String = ""): String {
        val newPropertyText = if(isKotlinFile) {
            if(isAfterText) {
                ",\n\n\t\t@get:Column(name = \"${convertPropNameToDbName(name)}\")\n\t\tvar $name: ${type.capitalize()}? = null \n"
            }
            else{
                "private val $name: ${type.capitalize()}? = null \n\n"
            }
        } else {"private ${if(type == "string") type.capitalize() else type} $name; \n\n\t"}
        return if(isAfterText) fileContent.substring(0, position + afterThisText.length) + newPropertyText + fileContent.substring(position + afterThisText.length) else fileContent.substring(0, position) + newPropertyText + fileContent.substring(position)
    }
}
