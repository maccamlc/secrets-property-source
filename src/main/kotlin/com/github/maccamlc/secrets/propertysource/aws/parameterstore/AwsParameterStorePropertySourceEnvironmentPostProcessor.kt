package com.github.maccamlc.secrets.propertysource.aws.parameterstore

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.github.maccamlc.secrets.propertysource.core.SecretsPropertySourceEnvironmentPostProcessor
import com.github.maccamlc.secrets.propertysource.core.SecretsSource
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceAccessor
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceConfiguration
import org.springframework.core.Ordered

internal class AwsParameterStorePropertySourceEnvironmentPostProcessor(
    override val secretsPropertySourceName: String = PROPERTY_STORE_PROPERTY_SOURCE_NAME,
    override val secretsPrefix: String = PREFIX_SECRET
) : SecretsPropertySourceEnvironmentPostProcessor(SecretsPropertySourceConfiguration.awsParameterStorePropertySourceEnabled) {

    override fun getOrder(): Int = ORDER

    private val defaultAwsSsm by lazy {
        AWSSimpleSystemsManagementClientBuilder.defaultClient()
    }

    override val secretsSource: SecretsSource = AwsParameterStoreSource({
        SecretsPropertySourceAccessor.awsSimpleSystemsManagement ?: defaultAwsSsm
    })

    companion object {

        private const val PROPERTY_STORE_PROPERTY_SOURCE_NAME = "AWSParameterStorePropertySource"
        private const val PREFIX_SECRET = "/aws-parameterstore/"

        const val ORDER = Ordered.HIGHEST_PRECEDENCE + 10
    }
}
