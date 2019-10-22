package com.github.maccamlc.secrets.propertysource.core

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment

abstract class SecretsPropertySourceEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) =
        SecretsPropertySource(
            name = secretsPropertySourceName,
            source = secretsSource,
            prefix = secretsPrefix
        ).run {
            environment.propertySources.addFirst(this)
        }

    abstract val secretsPropertySourceName: String

    abstract val secretsSource: SecretsSource

    abstract val secretsPrefix: String
}
