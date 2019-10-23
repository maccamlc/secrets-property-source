package com.github.maccamlc.secrets.propertysource.aws.secretsmanager

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import com.github.maccamlc.secrets.propertysource.core.SecretsSource
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceAccessor
import java.time.Duration
import org.slf4j.LoggerFactory

internal class AwsSecretsManagerSource(
    internal val awsSecretsManagerSupplier: () -> AWSSecretsManager,
    ticker: Ticker = Ticker.systemTicker()
) : SecretsSource {

    private val loadingCache = Caffeine.newBuilder()
        .ticker(ticker)
        .expireAfterWrite(Duration.ofMinutes(1))
        .build<String, String> { secretName ->
            try {
                GetSecretValueRequest().withSecretId(secretName)
                    .let { awsSecretsManagerSupplier().getSecretValue(it) }
                    .secretString
            } catch (e: ResourceNotFoundException) {
                logger.warn("No resource found in AWS Secret Manager for $secretName", e)
                null
            }
        }

    override fun getSecret(propertyName: String): String? =
        propertyName.split(SEPARATOR_JSON_PROPERTY_NAME)
            .let { propertyNameSplit ->
                when (propertyNameSplit.size) {
                    1 -> loadingCache.get(propertyName)
                    2 -> loadingCache.get(propertyNameSplit[0])
                        ?.let { (SecretsPropertySourceAccessor.objectMapper ?: defaultObjectMapper).readTree(it) }
                        ?.get(propertyNameSplit[1])
                        ?.let {
                            checkNotNull(
                                it.textValue(),
                                { "Text Value is null for ${propertyNameSplit[1]} in $propertyName" })
                        }
                    else -> throw IllegalStateException("Invalid secret name of $propertyName as too many JSON separators '$SEPARATOR_JSON_PROPERTY_NAME' producing $propertyNameSplit")
                }
            }

    companion object {

        private const val SEPARATOR_JSON_PROPERTY_NAME = "."

        private val logger = LoggerFactory.getLogger(AwsSecretsManagerSource::class.java)
        private val defaultObjectMapper by lazy { ObjectMapper() }
    }
}
