package com.github.maccamlc.secrets.propertysource.aws.secretsmanager

import ch.tutteli.atrium.api.fluent.en_GB.contains
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.assertThat
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MutablePropertySources

internal class AwsSecretsManagerPropertySourceEnvironmentPostProcessorTest {

    private val configurableEnvironment: ConfigurableEnvironment = mockk()

    private val springApplication: SpringApplication = mockk()

    private val propertySource: MutablePropertySources = MutablePropertySources()

    private lateinit var secretsPropertySourceEnvironmentPostProcessor: AwsSecretsManagerPropertySourceEnvironmentPostProcessor

    @BeforeEach
    internal fun setup() {
        every { configurableEnvironment.propertySources } returns propertySource

        secretsPropertySourceEnvironmentPostProcessor = AwsSecretsManagerPropertySourceEnvironmentPostProcessor()
    }

    @Test
    internal fun `should have expected name`() {
        assertThat(secretsPropertySourceEnvironmentPostProcessor.secretsPropertySourceName).toBe("AWSSecretsManagerPropertySource")
    }

    @Test
    internal fun `should have expected prefix`() {
        assertThat(secretsPropertySourceEnvironmentPostProcessor.secretsPrefix).toBe("/aws-secretsmanager/")
    }

    @Test
    internal fun `should have expected source that provides AWS Secrets Manager`() {
        assertThat(secretsPropertySourceEnvironmentPostProcessor.secretsSource)
            .isA<AwsSecretsManagerSource> {
            feature({ p(it::awsSecretsManagerSupplier) }) {
                feature({ f(it::invoke) }) {
                    feature { f(it::toString) }.contains("com.amazonaws.services.secretsmanager.AWSSecretsManagerClient")
                }
            }
        }
    }

    @Test
    internal fun `should add property source to beginning of environment`() {
        secretsPropertySourceEnvironmentPostProcessor.postProcessEnvironment(configurableEnvironment, springApplication)

        verify { configurableEnvironment.propertySources }
        verify { springApplication wasNot Called }
        confirmVerified(configurableEnvironment, springApplication)

        assertThat(propertySource) {
            feature { f(it::size) }.toBe(1)
        }
    }
}
