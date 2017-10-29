package com.raybritton.jsonquery

import com.google.gson.Gson
import com.google.gson.JsonParseException
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.util.prefs.Preferences

class SaveLoadDialogs(private val stage: Stage, private val gson: Gson) {
    private val PREFS_SAVE_DIR = "saves.directory"
    private val PREFS_OPEN_DIR = "json.directory"
    private val prefs = Preferences.userNodeForPackage(this.javaClass)

    fun openJsonFile(): String? {
        val dlg = FileChooser()
        dlg.title = "Open JSON file"
        val path = prefs.get(PREFS_OPEN_DIR, File(System.getProperty("user.home"), "Downloads").absolutePath)
        dlg.initialDirectory = File(path)
        dlg.extensionFilters.addAll(
                FileChooser.ExtensionFilter("JSON File", "*.json"),
                FileChooser.ExtensionFilter("Plain text file", "*.txt"),
                FileChooser.ExtensionFilter("Any file", "*.*")
        )
        val selectedFile: File? = dlg.showOpenDialog(stage)
        return selectedFile?.let {
            prefs.put(PREFS_OPEN_DIR, selectedFile.parentFile.absolutePath)
            val json = selectedFile.readLines().joinToString(separator = System.lineSeparator())
            return try {
                gson.fromJson(json, Any::class.java)
                json
            } catch (e: JsonParseException) {
                val alert = Alert(Alert.AlertType.ERROR, e.message, ButtonType.OK)
                alert.title = "Invalid JSON"
                alert.show()
                null
            }
        }
    }

    fun saveOutput(content: String) {
        val dlg = FileChooser()
        dlg.title = "Save results"
        val path = prefs.get(PREFS_SAVE_DIR, File(System.getProperty("user.home"), "Downloads").absolutePath)
        dlg.initialDirectory = File(path)
        dlg.extensionFilters.addAll(
                FileChooser.ExtensionFilter("Plain text file", "*.txt"),
                FileChooser.ExtensionFilter("Any file", "*.*")
        )
        val selectedFile: File? = dlg.showSaveDialog(stage)
        selectedFile?.let {
            prefs.put(PREFS_SAVE_DIR, selectedFile.parentFile.absolutePath)
            selectedFile.writeText(content)
        }
    }
}
