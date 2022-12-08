package com.khanivorous.todowebclient.service

import com.khanivorous.todowebclient.model.Todo
import org.springframework.stereotype.Service

@Service
interface TodoClient {
    fun getTodoById(id: Int): Todo?
}