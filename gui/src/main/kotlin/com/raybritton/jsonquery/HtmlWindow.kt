package com.raybritton.jsonquery

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.web.WebView
import javafx.stage.Stage

class HtmlWindow() {
    fun show(title: String, url: String) {
        val root = FXMLLoader.load<Parent>(javaClass.classLoader.getResource("html_window.fxml"))
        val scene = Scene(root, 800.0, 600.0)
        val stage = Stage()
        stage.title = title
        stage.scene = scene
        stage.isMaximized = true
        stage.show()
        (scene.lookup("#webview") as WebView).engine.load(url)
    }
}