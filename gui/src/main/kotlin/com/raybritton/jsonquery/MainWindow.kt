package com.raybritton.jsonquery

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.raybritton.jsonquery.models.Query
import com.raybritton.jsonquery.tools.toQuery
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
import javafx.stage.Stage

class MainWindow : Application() {

    private val jsonQuery = JsonQuery()
    private val queryBuilder = QueryBuilder()
    private val whereBuilder = WhereBuilder()
    private lateinit var stage: Stage

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val saveLoadDialogs by lazy { SaveLoadDialogs(stage, gson) }

    private lateinit var scene: Scene
    private val outputSplit by lazy { scene.lookup("#split_output") as TextArea }
    private val outputTab by lazy { scene.lookup("#tab_output") as TextArea }
    private val jsonSplit by lazy { scene.lookup("#split_json") as TextArea }
    private val jsonTab by lazy { scene.lookup("#tab_json") as TextArea }
    private val advancedQueryField by lazy { scene.lookup("#advanced_query") as TextField }
    private val queryField by lazy { scene.lookup("#query_field") as TextField }
    private val selectEverything by lazy { scene.lookup("#query_everything") as CheckBox }
    private val splitView by lazy { scene.lookup("#splits_enabled") as CheckBox }
    private val advancedMode by lazy { scene.lookup("#advanced_mode") as CheckBox }
    private val tabs by lazy { scene.lookup("#tabs") }
    private val splits by lazy { scene.lookup("#splits") }
    private val whereCheck by lazy { scene.lookup("#where_enabled") as CheckBox }
    private val simple by lazy { scene.lookup("#simple_query") }
    private val advanced by lazy { scene.lookup("#advanced_input") }
    private val queryBox by lazy { scene.lookup("#query_box") }
    private val limit by lazy { scene.lookup("#limit") as TextField }
    private val offset by lazy { scene.lookup("#offset") as TextField }
    private val orderBy by lazy { scene.lookup("#order_by_field") as TextField }
    private val orderDesc by lazy { scene.lookup("#order_by_desc") as CheckBox }
    private val searchBox by lazy { scene.lookup("#search_box") }
    private val searchValue by lazy { scene.lookup("#search_value") as TextField}
    private val searchTarget by lazy { scene.lookup("#search_target") as TextField}
    private val whereField by lazy { scene.lookup("#where_field") as TextField}
    private val whereCompare by lazy { scene.lookup("#where_compare") as TextField}
    private val targetBox by lazy { scene.lookup("#target_box") }
    private val checksBox by lazy { scene.lookup("#checks_box") }
    private val extrasBox by lazy { scene.lookup("#extras_box") }
    private val distinct by lazy { scene.lookup("#distinct") as CheckBox }
    private val asJson by lazy { scene.lookup("#as_json") as CheckBox }
    private val prettyPrint by lazy { scene.lookup("#pretty") as CheckBox }
    private val withKeys by lazy { scene.lookup("#with_keys") as CheckBox }
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
        scene.lookup("#load_json").setOnMouseClicked { jsonTab.text = saveLoadDialogs.openJsonFile() }
        scene.lookup("#save_output").setOnMouseClicked { saveLoadDialogs.saveOutput(outputTab.text) }
        scene.lookup("#split_format").setOnMouseClicked { formatJson() }
        scene.lookup("#tab_format").setOnMouseClicked { formatJson() }
        scene.lookup("#help").setOnMouseClicked { HtmlWindow().show("Help", "https://github.com/raybritton/json-query/blob/master/JQL.md") }

        splitView.selectedProperty().set(true)
    }

    private fun formatJson() {
        if (jsonTab.text.isNullOrEmpty()) return
        try {
            val obj = gson.fromJson(jsonTab.text, Any::class.java)
            jsonTab.text = gson.toJson(obj)
        } catch (e: JsonParseException) {
            val alert = Alert(Alert.AlertType.ERROR, e.message, ButtonType.OK)
            alert.title = "Invalid JSON"
            alert.show()
        }
    }

    private fun fillQueryUI() {
        val queryObject = advancedQueryField.text.toQuery()
        method.value = queryObject.method.name
        if (queryObject.method == Query.Method.SEARCH) {
            searchTarget.text = queryObject.target
            searchValue.text = queryObject.targetKeys.joinToString(",")
            searchMod.value = queryObject.targetExtra?.name
        } else {
            if (queryObject.target == "." && queryObject.targetKeys.isEmpty()) {
                selectEverything.isSelected = true
            } else {
                selectEverything.isSelected = false
                queryMod.value = queryObject.targetExtra?.name
                queryField.text = queryObject.targetKeys.joinToString(", ")
                queryTarget.text = queryObject.target
            }
            limit.text = queryObject.limit?.toString() ?: ""
            offset.text = queryObject.offset?.toString() ?: ""
            orderBy.text = queryObject.order ?: ""
            orderDesc.isSelected = queryObject.desc
            if (queryObject.where == null) {
                whereCheck.isSelected = false
            } else {
                whereCheck.isSelected = true
                whereField.text = queryObject.where?.field
                whereCompare.text = queryObject.where?.compare.toString()
                operator.value = queryObject.where?.operator.toString().replace('_',' ')
            }
        }
        asJson.isSelected = queryObject.asJson
        distinct.isSelected = queryObject.distinct
        prettyPrint.isSelected = queryObject.pretty
        withKeys.isSelected = queryObject.withKeys
    }

    private fun buildTextQuery() {
        queryBuilder.method = Query.Method.valueOf(method.value)
        if (queryBuilder.method == Query.Method.SELECT || queryBuilder.method == Query.Method.DESCRIBE ) {
            if (selectEverything.isSelected) {
                queryBuilder.target = "."
            } else {
                queryBuilder.target = queryTarget.text
                queryBuilder.keys = queryField.text.split(",")
                queryBuilder.extra = Query.TargetExtra.valueOf(queryMod.value)
            }
        } else {
            queryBuilder.target = searchTarget.text
            queryBuilder.extra = Query.TargetExtra.valueOf(searchMod.value)
            queryBuilder.keys = searchValue.text.split(',')
        }

        queryBuilder.limit = limit.text.toIntOrNull()
        queryBuilder.offset = offset.text.toIntOrNull()
        if (whereCheck.isSelected && whereField.text.isNotEmpty() && whereCompare.text.isNotEmpty() && operator.value.isNotEmpty()) {
            val whereBuilder = WhereBuilder()
            whereBuilder.compare = whereCompare.text
            whereBuilder.field = whereField.text
            whereBuilder.operator = Query.Where.Operator.valueOf(operator.value.replace(' ', '_'))
            queryBuilder.where = whereBuilder.build()
        } else {
            queryBuilder.where = null
        }
        if (orderBy.text.isNotEmpty()) {
            queryBuilder.order = orderBy.text
            queryBuilder.desc = orderDesc.isSelected
        } else {
            queryBuilder.order = null
            queryBuilder.desc = false
        }
        queryBuilder.asJson = asJson.isSelected
        queryBuilder.distinct = distinct.isSelected
        queryBuilder.pretty = prettyPrint.isSelected
        queryBuilder.withKeys = withKeys.isSelected

        if (queryBuilder.method != null && queryBuilder.target != null) {
            advancedQueryField.text = queryBuilder.build().toString()
        }
    }

    private fun process() {
        val json = jsonTab.text
        val query = advancedQueryField.text
        if (json.isNullOrEmpty() || query.isNullOrEmpty()) return
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

    private fun setupUi() {
        whereCheck.selectedProperty().addListener { _, _, checked ->
            whereBox.showOrHide(checked)
        }

        setupDropdowns()
        setupCheckboxes()

        scene.lookup("#advanced_run").setOnMouseClicked {
            fillQueryUI()
            process() }
        scene.lookup("#simple_run").setOnMouseClicked {
            buildTextQuery()
            process() }
        scene.lookup("#search_run").setOnMouseClicked {
            buildTextQuery()
            process() }

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
                extrasBox.isVisible = false
                checksBox.hide()
            } else {
                searchBox.hide()
                queryBox.show()
                extrasBox.isVisible = true
                whereCheck.show()
                checksBox.show()
                if (whereCheck.isSelected) {
                    whereBox.show()
                }
            }
        }

        queryMod.valueProperty().addListener { _, _, value ->
            when (value) {
                "KEYS", "VALUES" -> queryField.hide()
                else -> queryField.show()
            }
        }

        splits.hide()
        advanced.hide()
        searchBox.hide()
        queryBox.hide()
        targetBox.hide()
        whereBox.hide()
        extrasBox.isVisible = false
        whereCheck.hide()
        checksBox.hide()
    }

    private fun setupDropdowns() {
        method.items.setAll("SELECT", "DESCRIBE", "SELECT")
        searchMod.items.setAll("KEY", "VALUE")
        queryMod.items.setAll("SPECIFIC", "KEYS", "VALUES", "MAX", "MIN", "COUNT", "SUM")
        operator.items.setAll("EQUAL", "NOT EQUAL", "LESS THAN", "GREATER THAN", "CONTAINS", "NOT CONTAINS")
    }

    private fun setupCheckboxes() {
        prettyPrint.selectedProperty().addListener { _, _, value ->
            if (value) {
                asJson.isSelected = true
            }
        }
        asJson.selectedProperty().addListener { _, _, value ->
            if (!value) {
                prettyPrint.isSelected = false
            }
        }
    }
}