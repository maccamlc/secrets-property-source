package com.github.maccamlc.secrets.propertysource.aws.parameterstore

import ch.tutteli.atrium.api.fluent.en_GB.messageContains
import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.assertThat
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult
import com.amazonaws.services.simplesystemsmanagement.model.InternalServerErrorException
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException
import com.amazonaws.services.simplesystemsmanagement.model.ParameterVersionNotFoundException
import com.github.benmanes.caffeine.cache.Ticker
import io.github.glytching.junit.extension.random.Random
import io.github.glytching.junit.extension.random.RandomBeansExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(value = [RandomBeansExtension::class])
internal class AwsParameterStoreSourceTest {

    private val awsSimpleSystemsManagement: AWSSimpleSystemsManagement = mockk()

    private lateinit var awsParameterStoreSource: AwsParameterStoreSource

    @BeforeEach
    internal fun setup() {
        awsParameterStoreSource = AwsParameterStoreSource({ awsSimpleSystemsManagement })
    }

    @Test
    internal fun `should get secret from AWS Parameter Store`(@Random property: String, @Random value: String) {
        val getParameterRequest: GetParameterRequest = GetParameterRequest().withName(property).withWithDecryption(true)
        val getParameterResult: GetParameterResult = GetParameterResult().withParameter(Parameter().withValue(value))

        every { awsSimpleSystemsManagement.getParameter(getParameterRequest) } returns getParameterResult

        assertThat(awsParameterStoreSource.getSecret(property)).notToBeNull {
            toBe(value)
        }
    }

    @Test
    internal fun `should get secret from AWS Parameter Store that is cached`(@Random property: String, @Random value: String, @Random value2: String) {
        val tick = AtomicLong(System.currentTimeMillis())

        awsParameterStoreSource = AwsParameterStoreSource(
            { awsSimpleSystemsManagement },
            Ticker {
                tick.get()
            }
        )

        val getParameterRequest: GetParameterRequest = GetParameterRequest().withName(property).withWithDecryption(true)
        val getParameterResult: GetParameterResult = GetParameterResult().withParameter(Parameter().withValue(value))
        val getParameterResult2: GetParameterResult = GetParameterResult().withParameter(Parameter().withValue(value2))

        every { awsSimpleSystemsManagement.getParameter(getParameterRequest) } returnsMany listOf(
            getParameterResult,
            getParameterResult2
        )

        assertThat(awsParameterStoreSource.getSecret(property)).notToBeNull {
            toBe(value)
        }

        assertThat(awsParameterStoreSource.getSecret(property)).notToBeNull {
            toBe(value)
        }

        verify(exactly = 1) { awsSimpleSystemsManagement.getParameter(any()) }

        tick.addAndGet(Duration.ofSeconds(61).toNanos())

        assertThat(awsParameterStoreSource.getSecret(property)).notToBeNull {
            toBe(value2)
        }

        verify(exactly = 2) { awsSimpleSystemsManagement.getParameter(any()) }
    }

    @Test
    internal fun `should return null if resource is not found`(@Random property: String, @Random message: String) {
        val getParameterRequest: GetParameterRequest = GetParameterRequest().withName(property).withWithDecryption(true)

        every { awsSimpleSystemsManagement.getParameter(getParameterRequest) } throws ParameterNotFoundException(message)

        assertThat(awsParameterStoreSource.getSecret(property)).toBe(null)
    }

    @Test
    internal fun `should return null if resource version is not found`(@Random property: String, @Random message: String) {
        val getParameterRequest: GetParameterRequest = GetParameterRequest().withName(property).withWithDecryption(true)

        every { awsSimpleSystemsManagement.getParameter(getParameterRequest) } throws ParameterVersionNotFoundException(
            message
        )

        assertThat(awsParameterStoreSource.getSecret(property)).toBe(null)
    }

    @Test
    internal fun `should return null if parameter result is null`(@Random property: String) {
        val getParameterRequest: GetParameterRequest = GetParameterRequest().withName(property).withWithDecryption(true)
        val getParameterResult: GetParameterResult = GetParameterResult().withParameter(null)

        every { awsSimpleSystemsManagement.getParameter(getParameterRequest) } returns getParameterResult

        assertThat(awsParameterStoreSource.getSecret(property)).toBe(null)
    }

    @Test
    internal fun `should return null if parameter value is not found`(@Random property: String) {
        val getParameterRequest: GetParameterRequest = GetParameterRequest().withName(property).withWithDecryption(true)
        val getParameterResult: GetParameterResult = GetParameterResult().withParameter(Parameter().withValue(null))

        every { awsSimpleSystemsManagement.getParameter(getParameterRequest) } returns getParameterResult

        assertThat(awsParameterStoreSource.getSecret(property)).toBe(null)
    }

    @Test
    internal fun `should throw exception if request to AWS fails`(@Random property: String, @Random message: String) {
        val getParameterRequest: GetParameterRequest = GetParameterRequest().withName(property).withWithDecryption(true)

        every { awsSimpleSystemsManagement.getParameter(getParameterRequest) } throws InternalServerErrorException(
            message
        )

        assertThat { awsParameterStoreSource.getSecret(property) }
            .toThrow<InternalServerErrorException> {
                messageContains(message)
            }
    }
}
