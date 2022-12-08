package com.khanivorous.todowebclient.controller

import com.khanivorous.todowebclient.model.Todo
import com.khanivorous.todowebclient.service.TodoClient
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/todo"])
class TodoController @Autowired constructor(todoClient: TodoClient) {

    private val todoClient: TodoClient

    init {
        this.todoClient = todoClient
    }

    @Operation(summary = "Get todo by id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "found todo",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Todo::class))]
            ),
            ApiResponse(responseCode = "404", description = "Todo not found")
        ]
    )
    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(
        HttpStatus.OK
    )
    @ResponseBody
    fun getUserById(@Parameter(description = "id of Todo to be searched") @PathVariable id: Int): Todo? {
        return todoClient.getTodoById(id)
    }
}