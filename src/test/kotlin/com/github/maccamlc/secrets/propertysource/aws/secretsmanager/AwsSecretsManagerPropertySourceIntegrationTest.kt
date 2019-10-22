package com.github.maccamlc.secrets.propertysource.aws.secretsmanager

import ch.tutteli.atrium.api.cc.en_GB.returnValueOf
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.maccamlc.secrets.propertysource.aws.localstack.SpringBootLocalstackIntegrationTest
import com.github.maccamlc.secrets.propertysource.aws.localstack.SpringBootLocalstackTestConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootLocalstackIntegrationTest
internal class AwsSecretsManagerPropertySourceIntegrationTest {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Test
    internal fun `should retrieve property value one from AWS Secrets Manager in localstack`(@Autowired objectMapper: ObjectMapper) {
        val response =
            testRestTemplate.getForEntity(
                "/property?name={name}",
                String::class.java,
                mapOf("name" to "param.secret.value1")
            )

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.OK)
            returnValueOf(subject::getBody).toBe(
                objectMapper.writeValueAsString(
                    mapOf(
                        SpringBootLocalstackTestConfiguration.secretKeyOne_1 to SpringBootLocalstackTestConfiguration.secretValueOne_1,
                        SpringBootLocalstackTestConfiguration.secretKeyOne_2 to SpringBootLocalstackTestConfiguration.secretValueOne_2
                    )
                )
            )
        }
    }

    @Test
    internal fun `should retrieve property JSON value one from AWS Secrets Manager in localstack`() {
        val response =
            testRestTemplate.getForEntity(
                "/property?name={name}",
                String::class.java,
                mapOf("name" to "param.secret.value1.key1")
            )

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.OK)
            returnValueOf(subject::getBody).toBe(SpringBootLocalstackTestConfiguration.secretValueOne_1)
        }
    }

    @Test
    internal fun `should retrieve property JSON value two from AWS Secrets Manager in localstack`() {
        val response =
            testRestTemplate.getForEntity(
                "/property?name={name}",
                String::class.java,
                mapOf("name" to "param.secret.value2.key")
            )

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.OK)
            returnValueOf(subject::getBody).toBe(SpringBootLocalstackTestConfiguration.secretValueTwo)
        }
    }

    @Test
    internal fun `should retrieve property value two from AWS Secrets Manager in localstack`(@Autowired objectMapper: ObjectMapper) {
        val response =
            testRestTemplate.getForEntity(
                "/property?name={name}",
                String::class.java,
                mapOf("name" to "param.secret.value2")
            )

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.OK)
            returnValueOf(subject::getBody).toBe(
                objectMapper.writeValueAsString(
                    mapOf(SpringBootLocalstackTestConfiguration.secretKeyTwo to SpringBootLocalstackTestConfiguration.secretValueTwo)
                )
            )
        }
    }

    @Test
    internal fun `should retrieve standard property value not in AWS Secrets Manager`() {
        val response = testRestTemplate.getForEntity(
            "/property?name={name}",
            String::class.java,
            mapOf("name" to "standard.value")
        )

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.OK)
            returnValueOf(subject::getBody).toBe(SpringBootLocalstackTestConfiguration.standard)
        }
    }

    @Test
    internal fun `should not return property that does not exist`() {
        val response =
            testRestTemplate.getForEntity("/property?name={name}", String::class.java, mapOf("name" to "unknown.value"))

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.NO_CONTENT)
        }
    }
}
