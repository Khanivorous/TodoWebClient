package com.khanivorous.todowebclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TodoWebClientApplication

fun main(args: Array<String>) {
    runApplication<TodoWebClientApplication>(*args)
}
