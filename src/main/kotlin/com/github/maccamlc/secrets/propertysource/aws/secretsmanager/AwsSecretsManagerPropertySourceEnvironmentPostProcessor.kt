package com.github.maccamlc.secrets.propertysource.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.github.maccamlc.secrets.propertysource.core.SecretsPropertySourceEnvironmentPostProcessor
import com.github.maccamlc.secrets.propertysource.core.SecretsSource
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceAccessor
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceConfiguration
import org.springframework.core.Ordered

class AwsSecretsManagerPropertySourceEnvironmentPostProcessor(
    override val secretsPropertySourceName: String = SECRETS_MANAGER_PROPERTY_SOURCE_NAME,
    override val secretsPrefix: String = PREFIX_SECRET
) : SecretsPropertySourceEnvironmentPostProcessor(SecretsPropertySourceConfiguration.awsSecretsManagerPropertySourceEnabled) {

    override fun getOrder(): Int = ORDER

    private val defaultAwsSecretsManager by lazy {
        AWSSecretsManagerClientBuilder.defaultClient()
    }

    override val secretsSource: SecretsSource = AwsSecretsManagerSource({
        SecretsPropertySourceAccessor.awsSecretsManager ?: defaultAwsSecretsManager
    })

    companion object {

        const val SECRETS_MANAGER_PROPERTY_SOURCE_NAME = "AWSSecretsManagerPropertySource"
        const val PREFIX_SECRET = "/aws-secretsmanager/"
        const val ORDER = Ordered.HIGHEST_PRECEDENCE + 10
    }
}
