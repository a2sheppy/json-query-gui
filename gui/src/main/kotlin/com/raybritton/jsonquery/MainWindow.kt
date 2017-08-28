package com.raybritton.jsonquery

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.Stage

class MainWindow : Application() {
    private lateinit var outputJson: TextArea
    private lateinit var inputJson: TextArea
    private lateinit var queryField: TextField

    private val jsonQuery = JsonQuery()

    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("window.fxml"))
        val scene = Scene(root, 800.0, 600.0)
        stage.title = "Json Query"
        stage.scene = scene
        stage.show()

        outputJson = scene.lookup("#output_json") as TextArea
        inputJson = scene.lookup("#input_json") as TextArea
        queryField = scene.lookup("#query") as TextField

        scene.lookup("#submit").setOnMouseClicked { process() }
    }

    private fun process() {
        val json = inputJson.text
        val query = queryField.text
        try {
            jsonQuery.loadJson(json)
            val output = jsonQuery.query(query)
            outputJson.text = output
        } catch (e: Exception) {
            outputJson.text = e.message
        }
    }
}