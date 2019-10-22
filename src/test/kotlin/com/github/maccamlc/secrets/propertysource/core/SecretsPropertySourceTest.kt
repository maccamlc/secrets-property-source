package com.github.maccamlc.secrets.propertysource.core

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.verbs.assertThat
import io.github.glytching.junit.extension.random.Random
import io.github.glytching.junit.extension.random.RandomBeansExtension
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(value = [RandomBeansExtension::class])
internal class SecretsPropertySourceTest {

    private val secretsSource: SecretsSource = mockk()

    @Random
    private lateinit var prefix: String

    @Random
    private lateinit var name: String

    private lateinit var secretsPropertySource: SecretsPropertySource

    @BeforeEach
    internal fun setup() {
        secretsPropertySource = SecretsPropertySource(
            name = name,
            source = secretsSource,
            prefix = prefix
        )
    }

    @Test
    internal fun `should set property source name`() {
        assertThat(secretsPropertySource.name).toBe(name)
    }

    @Test
    internal fun `should set property source object`() {
        assertThat(secretsPropertySource.source).toBe(secretsSource)
    }

    @Test
    internal fun `should set property prefix`() {
        assertThat(secretsPropertySource.prefix).toBe(prefix)
    }

    @Test
    internal fun `should return property value when name starts with prefix`(@Random propertyName: String, @Random propertyValue: String) {
        every { secretsSource.getSecret(propertyName) } returns propertyValue

        assertThat(secretsPropertySource.getProperty("$prefix$propertyName")).toBe(propertyValue)

        verify { secretsSource.getSecret(any()) }
    }

    @Test
    internal fun `should return null when no property when name starts with prefix`(@Random propertyName: String) {
        every { secretsSource.getSecret(propertyName) } returns null

        assertThat(secretsPropertySource.getProperty("$prefix$propertyName")).toBe(null)

        verify { secretsSource.getSecret(any()) }
    }

    @Test
    internal fun `should return null when name does not start with prefix`(@Random propertyName: String) {
        assertThat(secretsPropertySource.getProperty(propertyName)).toBe(null)

        verify { secretsSource wasNot Called }
    }

    @Test
    internal fun `should ensure that contains property returns true when property found`(@Random propertyName: String, @Random propertyValue: String) {
        every { secretsSource.getSecret(propertyName) } returns propertyValue

        assertThat(secretsPropertySource.containsProperty("$prefix$propertyName")).toBe(true)
    }

    @Test
    internal fun `should ensure that contains property returns false when property not found`(@Random propertyName: String) {
        assertThat(secretsPropertySource.containsProperty(propertyName)).toBe(false)
    }
}
