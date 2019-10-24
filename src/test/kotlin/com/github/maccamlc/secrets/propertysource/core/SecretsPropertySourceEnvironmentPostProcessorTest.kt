package com.github.maccamlc.secrets.propertysource.core

import ch.tutteli.atrium.api.cc.en_GB.containsExactly
import ch.tutteli.atrium.api.cc.en_GB.isA
import ch.tutteli.atrium.api.cc.en_GB.property
import ch.tutteli.atrium.api.cc.en_GB.returnValueOf
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import io.github.glytching.junit.extension.random.Random
import io.github.glytching.junit.extension.random.RandomBeansExtension
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertySource

@ExtendWith(value = [RandomBeansExtension::class])
internal class SecretsPropertySourceEnvironmentPostProcessorTest {

    private val configurableEnvironment: ConfigurableEnvironment = mockk()

    private val springApplication: SpringApplication = mockk()

    private val propertySource: MutablePropertySources = mockk(relaxed = true)

    private val source: SecretsSource = mockk()

    @Random
    private lateinit var prefix: String

    @Random
    private lateinit var name: String

    private lateinit var secretsPropertySourceEnvironmentPostProcessor: SecretsPropertySourceEnvironmentPostProcessor

    @BeforeEach
    internal fun setup() {
        every { configurableEnvironment.propertySources } returns propertySource

        secretsPropertySourceEnvironmentPostProcessor = object : SecretsPropertySourceEnvironmentPostProcessor(true) {
            override val secretsPropertySourceName: String
                get() = name
            override val secretsSource: SecretsSource
                get() = source
            override val secretsPrefix: String
                get() = prefix
        }
    }

    @Test
    internal fun `should have expected name`() {
        assertThat(secretsPropertySourceEnvironmentPostProcessor.secretsPropertySourceName).toBe(name)
    }

    @Test
    internal fun `should have expected prefix`() {
        assertThat(secretsPropertySourceEnvironmentPostProcessor.secretsPrefix).toBe(prefix)
    }

    @Test
    internal fun `should have expected source`() {
        assertThat(secretsPropertySourceEnvironmentPostProcessor.secretsSource).toBe(source)
    }

    @Test
    internal fun `should add property source to beginning of environment`() {
        secretsPropertySourceEnvironmentPostProcessor.postProcessEnvironment(configurableEnvironment, springApplication)

        val propertySourceList = mutableListOf<PropertySource<Any>>()

        verify { configurableEnvironment.propertySources }
        verify { propertySource.addFirst(capture(propertySourceList)) }
        verify { springApplication wasNot Called }
        confirmVerified(configurableEnvironment, propertySource, springApplication)

        assertThat(propertySourceList)
            .containsExactly {
                isA<SecretsPropertySource> {
                    returnValueOf(subject::getSource).toBe(source)
                    returnValueOf(subject::getName).toBe(name)
                    property(subject::prefix).toBe(prefix)
                }
            }
    }

    @Test
    internal fun `should not add property source to beginning of environment when disabled`() {
        val disabledSecretsPropertySourceEnvironmentPostProcessor =
            object : SecretsPropertySourceEnvironmentPostProcessor(false) {
                override val secretsPropertySourceName: String
                    get() = name
                override val secretsSource: SecretsSource
                    get() = source
                override val secretsPrefix: String
                    get() = prefix
            }

        disabledSecretsPropertySourceEnvironmentPostProcessor.postProcessEnvironment(
            configurableEnvironment,
            springApplication
        )

        verify { configurableEnvironment wasNot Called }
        verify { springApplication wasNot Called }
        confirmVerified(configurableEnvironment, propertySource, springApplication)
    }
}
