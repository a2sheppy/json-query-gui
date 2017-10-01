package com.raybritton.jsonquery

import javafx.scene.Node

fun Node.show() {
    isVisible = true
    isManaged = true
}

fun Node.hide() {
    isVisible = false
    isManaged = false
}

fun Node.showOrHide(show: Boolean) {
    isVisible = show
    isManaged = show
}