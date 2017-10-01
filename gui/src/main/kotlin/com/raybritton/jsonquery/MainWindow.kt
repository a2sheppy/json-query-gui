package com.raybritton.jsonquery

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.raybritton.jsonquery.models.Query
import com.raybritton.jsonquery.models.Query.Method.*
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class MainWindow : Application() {
    private val jsonQuery = JsonQuery()
    private val queryBuilder = QueryBuilder()
    private val whereBuilder = WhereBuilder()
    private lateinit var stage: Stage

    private lateinit var scene: Scene
    private val outputSplit by lazy { scene.lookup("#split_output") as TextArea }
    private val outputTab by lazy { scene.lookup("#tab_output") as TextArea }
    private val jsonSplit by lazy { scene.lookup("#split_json") as TextArea }
    private val jsonTab by lazy { scene.lookup("#tab_json") as TextArea }
    private val queryField by lazy { scene.lookup("#advanced_query") as TextField }
    private val selectEverything by lazy { scene.lookup("#query_everything") as CheckBox }
    private val splitView by lazy { scene.lookup("#splits_enabled") as CheckBox }
    private val advancedMode by lazy { scene.lookup("#advanced_mode") as CheckBox }
    private val tabs by lazy { scene.lookup("#tabs") }
    private val splits by lazy { scene.lookup("#splits") }
    private val simple by lazy { scene.lookup("#simple_query") }
    private val advanced by lazy { scene.lookup("#advanced_input") }
    private val queryBox by lazy { scene.lookup("#query_box") }
    private val searchBox by lazy { scene.lookup("#search_box") }
    private val targetBox by lazy { scene.lookup("#target_box") }
    private val checksBox by lazy { scene.lookup("#checks_box") }
    private val extrasBox by lazy { scene.lookup("#extras_box") }
    private val queryTarget by lazy { scene.lookup("#query_target") as TextField }
    private val whereBox by lazy { scene.lookup("#where_box") }
    private val method by lazy { scene.lookup("#method") as ComboBox<String> }
    private val searchMod by lazy { scene.lookup("#search_modifier") as ChoiceBox<String> }
    private val queryMod by lazy { scene.lookup("#query_modifier") as ChoiceBox<String> }
    private val operator by lazy { scene.lookup("#where_operator") as ComboBox<String> }

    override fun start(stage: Stage) {
        this.stage = stage
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("jq_gui.fxml"))
        scene = Scene(root, 800.0, 600.0)
        stage.title = "Json Query"
        stage.scene = scene
        stage.isMaximized = true
        stage.show()

        setupUi()

        jsonTab.textProperty().bindBidirectional(jsonSplit.textProperty())
        outputTab.textProperty().bindBidirectional(outputSplit.textProperty())
        scene.lookup("#load_json").setOnMouseClicked { openJsonFile() }
        scene.lookup("#save_output").setOnMouseClicked { saveOutput() }

        splitView.selectedProperty().set(true)
    }

    private fun buildTextQuery() {
        queryBuilder.method = valueOf(method.value)
        if (queryBuilder.method == SELECT || queryBuilder.method == DESCRIBE ) {
            if (selectEverything.isSelected) {
                queryBuilder.target = "."
            } else {
                queryBuilder.target = queryTarget.text
            }
        }

        if (queryBuilder.method != null && queryBuilder.target != null) {

            queryField.text = queryBuilder.build().toString()
        }
    }

    private fun process() {
        buildTextQuery()
        val json = jsonTab.text
        val query = queryField.text
        processQuery(query, json)
    }

    private fun processQuery(query: String, json: String) {
        jsonQuery.loadJson(json)
        try {
            val result = jsonQuery.query(query)
            showResult(result)
        } catch (e: Exception) {
            showResult(e.message ?: e.javaClass.simpleName)
        }
    }

    private fun showResult(msg: String) {
        outputTab.text = msg
    }

    private fun openJsonFile() {
        val dlg = FileChooser()
        dlg.title = "Open JSON file"
        dlg.initialDirectory = File(System.getProperty("user.home"), "Downloads")
        dlg.extensionFilters.addAll(
                FileChooser.ExtensionFilter("JSON File", "*.json"),
                FileChooser.ExtensionFilter("Plain text file", "*.txt"),
                FileChooser.ExtensionFilter("Any file", "*.*")
        )
        val selectedFile: File? = dlg.showOpenDialog(stage)
        selectedFile?.let {
            val json = selectedFile.readLines().joinToString(separator = System.lineSeparator())
            try {
                Gson().fromJson(json, Any::class.java)
                jsonTab.text = json
            } catch (e: JsonParseException) {
                val alert = Alert(Alert.AlertType.ERROR, e.message, ButtonType.OK)
                alert.title = "Invalid JSON"
                alert.show()
            }
        }
    }

    private fun saveOutput() {
        val dlg = FileChooser()
        dlg.title = "Save results"
        dlg.initialDirectory = File(System.getProperty("user.home"), "Downloads")
        dlg.extensionFilters.addAll(
                FileChooser.ExtensionFilter("Plain text file", "*.txt"),
                FileChooser.ExtensionFilter("Any file", "*.*")
        )
        val selectedFile: File? = dlg.showSaveDialog(stage)
        selectedFile?.let {
            selectedFile.writeText(outputTab.text)
        }
    }

    private fun setupUi() {
        val whereCheck = scene.lookup("#where_enabled") as CheckBox
        whereCheck.selectedProperty().addListener { _, _, checked ->
            whereBox.showOrHide(checked)
        }

        setupDropdowns()

        scene.lookup("#advanced_run").setOnMouseClicked { process() }
        scene.lookup("#simple_run").setOnMouseClicked { process() }

        selectEverything.selectedProperty().addListener { _, _, checked ->
            targetBox.showOrHide(!checked)
        }

        splitView.selectedProperty().addListener { _, _, checked ->
            splits.showOrHide(checked)
            tabs.showOrHide(!checked)
        }

        advancedMode.selectedProperty().addListener { _, _, checked ->
            advanced.showOrHide(checked)
            simple.showOrHide(!checked)
        }

        method.valueProperty().addListener { _, _, value ->
            if (value == "SEARCH") {
                searchBox.show()
                queryBox.hide()
                whereBox.hide()
                whereCheck.hide()
                extrasBox.hide()
                checksBox.hide()
                whereCheck.selectedProperty().set(false)
                whereCheck.disableProperty().set(true)
            } else {
                searchBox.hide()
                queryBox.show()
                whereBox.show()
                extrasBox.show()
                whereCheck.show()
                checksBox.show()
                whereCheck.disableProperty().set(false)
            }
        }

        splits.hide()
        advanced.hide()
        searchBox.hide()
        queryBox.hide()
        targetBox.hide()
        whereBox.hide()
        extrasBox.hide()
        whereCheck.hide()
        checksBox.hide()
    }

    private fun setupDropdowns() {
        method.items.setAll("SELECT", "DESCRIBE", "SEARCH")
        searchMod.items.setAll("KEY", "VALUE")
        queryMod.items.setAll("", "KEYS", "VALUES", "MAX", "MIN", "COUNT", "SUM")
        operator.items.setAll("LESS THAN", "GREATER THAN", "EQUAL", "NOT EQUAL", "CONTAIN", "NOT CONTAIN")
    }
}