package com.github.maccamlc.secrets.propertysource.aws.parameterstore

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException
import com.amazonaws.services.simplesystemsmanagement.model.ParameterVersionNotFoundException
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import com.github.maccamlc.secrets.propertysource.core.SecretsSource
import com.github.maccamlc.secrets.propertysource.shared.SecretsPropertySourceConfiguration
import org.slf4j.LoggerFactory

internal class AwsParameterStoreSource(
    internal val awsSimpleSystemsManagementSupplier: () -> AWSSimpleSystemsManagement,
    ticker: Ticker = Ticker.systemTicker()
) : SecretsSource {

    private val loadingCache = Caffeine.newBuilder()
        .ticker(ticker)
        .also { builder ->
            SecretsPropertySourceConfiguration.cacheExpiry
                ?.run { builder.expireAfterWrite(this) }
        }
        .build<String, String> { parameterName ->
            try {
                GetParameterRequest().withName(parameterName).withWithDecryption(true)
                    .let { awsSimpleSystemsManagementSupplier().getParameter(it) }
                    ?.parameter
                    ?.value
            } catch (e: ParameterNotFoundException) {
                logger.warn("No parameter found in AWS Parameter Store for $parameterName", e)
                null
            } catch (e: ParameterVersionNotFoundException) {
                logger.warn("No parameter version found in AWS Parameter Store for $parameterName", e)
                null
            }
        }

    override fun getSecret(propertyName: String): String? = loadingCache.get(propertyName)

    companion object {

        private val logger = LoggerFactory.getLogger(AwsParameterStoreSource::class.java)
    }
}
