package com.github.maccamlc.secrets.propertysource.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.github.maccamlc.secrets.propertysource.core.SecretsPropertySourceEnvironmentPostProcessor
import com.github.maccamlc.secrets.propertysource.core.SecretsSource
import com.github.maccamlc.secrets.propertysource.shared.PropertySourceAccessor
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment

internal class AwsSecretsManagerPropertySourceEnvironmentPostProcessor(
    override val secretsPropertySourceName: String = SECRETS_MANAGER_PROPERTY_SOURCE_NAME,
    override val secretsSource: SecretsSource = AwsSecretsManagerSource({
        PropertySourceAccessor.getAwsSecretsManager() ?: defaultAwsSecretsManager
    }),
    override val secretsPrefix: String = PREFIX_SECRET
) : SecretsPropertySourceEnvironmentPostProcessor() {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        if (!initialized) {
            super.postProcessEnvironment(environment, application)
            initialized = true
        }
    }

    companion object {
        private var initialized = false

        private const val SECRETS_MANAGER_PROPERTY_SOURCE_NAME = "AWSSecretsManagerPropertySource"
        private const val PREFIX_SECRET = "/aws-secretsmanager/"

        private val defaultAwsSecretsManager by lazy {
            AWSSecretsManagerClientBuilder.defaultClient()
        }
    }
}
