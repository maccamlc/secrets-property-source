package com.github.maccamlc.secrets.propertysource.aws.parameterstore

import ch.tutteli.atrium.api.cc.en_GB.returnValueOf
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import com.github.maccamlc.secrets.propertysource.aws.localstack.SpringBootLocalstackIntegrationTest
import com.github.maccamlc.secrets.propertysource.aws.localstack.SpringBootLocalstackTestConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootLocalstackIntegrationTest
internal class AwsParameterStorePropertySourceIntegrationTest {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Test
    internal fun `should retrieve property value one from AWS SSM in localstack`() {
        val response =
            testRestTemplate.getForEntity(
                "/property?name={name}",
                String::class.java,
                mapOf("name" to "param.store.value1")
            )

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.OK)
            returnValueOf(subject::getBody).toBe(SpringBootLocalstackTestConfiguration.parameterValueOne)
        }
    }

    @Test
    internal fun `should retrieve property value two from AWS SSM in localstack`() {
        val response =
            testRestTemplate.getForEntity(
                "/property?name={name}",
                String::class.java,
                mapOf("name" to "param.store.value2")
            )

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.OK)
            returnValueOf(subject::getBody).toBe(SpringBootLocalstackTestConfiguration.parameterValueTwo)
        }
    }

    @Test
    internal fun `should retrieve standard property value not in AWS SSM`(@Autowired testRestTemplate: TestRestTemplate) {
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
    internal fun `should not return property that does not exist`(@Autowired testRestTemplate: TestRestTemplate) {
        val response =
            testRestTemplate.getForEntity("/property?name={name}", String::class.java, mapOf("name" to "unknown.value"))

        assertThat(response) {
            returnValueOf(subject::getStatusCode).toBe(HttpStatus.NO_CONTENT)
        }
    }
}
