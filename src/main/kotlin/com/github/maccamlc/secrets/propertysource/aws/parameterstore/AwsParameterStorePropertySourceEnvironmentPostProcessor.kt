package com.github.maccamlc.secrets.propertysource.aws.parameterstore

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.github.maccamlc.secrets.propertysource.core.SecretsPropertySourceEnvironmentPostProcessor
import com.github.maccamlc.secrets.propertysource.core.SecretsSource
import com.github.maccamlc.secrets.propertysource.shared.PropertySourceAccessor
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment

internal class AwsParameterStorePropertySourceEnvironmentPostProcessor(
    override val secretsPropertySourceName: String = PROPERTY_STORE_PROPERTY_SOURCE_NAME,
    override val secretsSource: SecretsSource = AwsParameterStoreSource({
        PropertySourceAccessor.getAwsSimpleSystemsManagement() ?: defaultAwsSsm
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
        private const val PROPERTY_STORE_PROPERTY_SOURCE_NAME = "AWSParameterStorePropertySource"
        private const val PREFIX_SECRET = "/aws-parameterstore/"

        private val defaultAwsSsm by lazy {
            AWSSimpleSystemsManagementClientBuilder.defaultClient()
        }
    }
}
