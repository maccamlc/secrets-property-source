package com.github.maccamlc.secrets.propertysource.aws.secretsmanager

import ch.tutteli.atrium.api.cc.en_GB.message
import ch.tutteli.atrium.api.cc.en_GB.messageContains
import ch.tutteli.atrium.api.cc.en_GB.notToBeNull
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.api.cc.en_GB.toThrow
import ch.tutteli.atrium.verbs.assertThat
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult
import com.amazonaws.services.secretsmanager.model.InvalidParameterException
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Ticker
import io.github.glytching.junit.extension.random.Random
import io.github.glytching.junit.extension.random.RandomBeansExtension
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(value = [RandomBeansExtension::class])
internal class AwsSecretsManagerSourceTest {

    private val awsSecretsManager: AWSSecretsManager = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper()

    private lateinit var awsSecretsManagerSource: AwsSecretsManagerSource

    @BeforeEach
    internal fun setup() {
        awsSecretsManagerSource = AwsSecretsManagerSource({ awsSecretsManager })
    }

    @Test
    internal fun `should get secret from AWS Secrets Manager`(@Random property: String, @Random value: String) {
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)
        val getSecretValueResult: GetSecretValueResult = GetSecretValueResult().withSecretString(value)

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } returns getSecretValueResult

        assertThat(awsSecretsManagerSource.getSecret(property)).notToBeNull {
            toBe(value)
        }
    }

    @Test
    internal fun `should get secret from AWS Secrets Manager that is cached`(@Random property: String, @Random value: String, @Random value2: String) {
        val tick = AtomicLong(System.currentTimeMillis())

        awsSecretsManagerSource = AwsSecretsManagerSource(
            { awsSecretsManager },
            Ticker {
                tick.get()
            }
        )

        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)
        val getSecretValueResult: GetSecretValueResult = GetSecretValueResult().withSecretString(value)
        val getSecretValueResult2: GetSecretValueResult = GetSecretValueResult().withSecretString(value2)

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } returnsMany listOf(
            getSecretValueResult,
            getSecretValueResult2
        )

        assertThat(awsSecretsManagerSource.getSecret(property)).notToBeNull {
            toBe(value)
        }

        assertThat(awsSecretsManagerSource.getSecret(property)).notToBeNull {
            toBe(value)
        }

        verify(exactly = 1) { awsSecretsManager.getSecretValue(any()) }

        tick.addAndGet(Duration.ofSeconds(61).toNanos())

        assertThat(awsSecretsManagerSource.getSecret(property)).notToBeNull {
            toBe(value2)
        }

        verify(exactly = 2) { awsSecretsManager.getSecretValue(any()) }
    }

    @Test
    internal fun `should get JSON value in secret from AWS Secrets Manager`(@Random property: String, @Random jsonParameter: String, @Random value: String) {
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)
        val getSecretValueResult: GetSecretValueResult =
            GetSecretValueResult().withSecretString(objectMapper.writeValueAsString(mapOf(jsonParameter to value)))

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } returns getSecretValueResult

        assertThat(awsSecretsManagerSource.getSecret("$property.$jsonParameter")).notToBeNull {
            toBe(value)
        }
    }

    @Test
    internal fun `should throw exception if secret in invalid format`(@Random property: String, @Random jsonParameter: String, @Random jsonParameter2: String) {
        assertThat { awsSecretsManagerSource.getSecret("$property.$jsonParameter.$jsonParameter2") }
            .toThrow<IllegalStateException> {
                message { toBe("Invalid secret name of $property.$jsonParameter.$jsonParameter2 as too many JSON separators '.' producing [$property, $jsonParameter, $jsonParameter2]") }
            }

        verify { awsSecretsManager wasNot Called }
    }

    @Test
    internal fun `should throw exception if JSON value in secret is not text`(@Random property: String, @Random jsonParameter: String, @Random value: Int) {
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)
        val getSecretValueResult: GetSecretValueResult =
            GetSecretValueResult().withSecretString(objectMapper.writeValueAsString(mapOf(jsonParameter to value)))

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } returns getSecretValueResult

        assertThat { awsSecretsManagerSource.getSecret("$property.$jsonParameter") }
            .toThrow<IllegalStateException> {
                message { toBe("Text Value is null for $jsonParameter in $property.$jsonParameter") }
            }
    }

    @Test
    internal fun `should return null if resource is not found`(@Random property: String, @Random message: String) {
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } throws ResourceNotFoundException(message)

        assertThat(awsSecretsManagerSource.getSecret(property)).toBe(null)
    }

    @Test
    internal fun `should return null if JSON resource is not found`(@Random property: String, @Random jsonParameter: String, @Random message: String) {
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } throws ResourceNotFoundException(message)

        assertThat(awsSecretsManagerSource.getSecret("$property.$jsonParameter")).toBe(null)
    }

    @Test
    internal fun `should return null if JSON key is not in resource`(@Random property: String, @Random jsonParameter: String, @Random value: String) {
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)
        val getSecretValueResult: GetSecretValueResult =
            GetSecretValueResult().withSecretString(objectMapper.writeValueAsString(mapOf(jsonParameter to value)))

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } returns getSecretValueResult

        assertThat(awsSecretsManagerSource.getSecret("$property.$jsonParameter-different")).toBe(null)
    }

    @Test
    internal fun `should throw exception if request to AWS fails`(@Random property: String, @Random message: String) {
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest().withSecretId(property)

        every { awsSecretsManager.getSecretValue(getSecretValueRequest) } throws InvalidParameterException(message)

        assertThat { awsSecretsManagerSource.getSecret(property) }
            .toThrow<InvalidParameterException> {
                messageContains(message)
            }
    }
}
