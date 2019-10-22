package com.github.maccamlc.secrets.propertysource.aws.localstack

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@Import(SpringBootLocalstackTestConfiguration::class)
open class TestApplication {

    @RestController
    class TestController @Autowired constructor(private val environment: Environment) {

        @GetMapping(path = ["/property"], produces = [MediaType.TEXT_PLAIN_VALUE])
        fun getPropertyValue(@RequestParam("name") name: String): ResponseEntity<String> {
            return environment.getProperty(name)
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity.noContent().build()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(TestApplication::class.java, *args)
        }
    }
}
