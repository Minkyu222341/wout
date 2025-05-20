package com.wout

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WoutApplication

fun main(args: Array<String>) {
    runApplication<WoutApplication>(*args)
}
