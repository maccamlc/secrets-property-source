package com.github.maccamlc.secrets.propertysource.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.github.maccamlc.secrets.propertysource.core.SecretsPropertySourceEnvironmentPostProcessor
import com.github.maccamlc.secrets.propertysource.core.SecretsSource
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceAccessor
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceConfiguration

internal class AwsSecretsManagerPropertySourceEnvironmentPostProcessor(
    override val secretsPropertySourceName: String = SECRETS_MANAGER_PROPERTY_SOURCE_NAME,
    override val secretsSource: SecretsSource = AwsSecretsManagerSource({
        SecretsPropertySourceAccessor.awsSecretsManager ?: defaultAwsSecretsManager
    }),
    override val secretsPrefix: String = PREFIX_SECRET
) : SecretsPropertySourceEnvironmentPostProcessor(SecretsPropertySourceConfiguration.awsSecretsManagerPropertySourceEnabled) {

    companion object {

        private const val SECRETS_MANAGER_PROPERTY_SOURCE_NAME = "AWSSecretsManagerPropertySource"
        private const val PREFIX_SECRET = "/aws-secretsmanager/"

        private val defaultAwsSecretsManager by lazy {
            AWSSecretsManagerClientBuilder.defaultClient()
        }
    }
}
