package com.github.maccamlc.secrets.propertysource.aws.localstack

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.ParameterType
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.random.Random.Default.nextInt
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphanumeric

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
internal open class SpringBootLocalstackTestConfiguration {

    @Bean
    internal open fun objectMapper() = ObjectMapper()

    @Bean
    internal open fun localstackAmazonSecretsManager(
        localStackContainer: LocalStackContainer,
        objectMapper: ObjectMapper
    ): AWSSecretsManager =
        AWSSecretsManagerClientBuilder.standard()
            .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(Service.SECRETSMANAGER))
            .withCredentials(localStackContainer.defaultCredentialsProvider)
            .build()
            .also {
                it.putSecretValue(
                    PutSecretValueRequest().withSecretId(SECRET_ONE).withSecretString(
                        objectMapper.writeValueAsString(
                            mapOf(secretKeyOne_1 to secretValueOne_1, secretKeyOne_2 to secretValueOne_2)
                        )
                    )
                )
                it.putSecretValue(
                    PutSecretValueRequest().withSecretId(SECRET_TWO).withSecretString(
                        objectMapper.writeValueAsString(
                            mapOf(secretKeyTwo to secretValueTwo)
                        )
                    )
                )
            }

    @Bean
    internal open fun localstackAmazonSSM(localStackContainer: LocalStackContainer): AWSSimpleSystemsManagement =
        AWSSimpleSystemsManagementClientBuilder.standard()
            .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(Service.SSM))
            .withCredentials(localStackContainer.defaultCredentialsProvider)
            .build()
            .also {
                it.putParameter(
                    PutParameterRequest().withName(SECRET_ONE).withValue(
                        parameterValueOne
                    ).withType(ParameterType.SecureString)
                )
                it.putParameter(
                    PutParameterRequest().withName(SECRET_TWO).withValue(
                        parameterValueTwo
                    ).withType(ParameterType.String)
                )
            }

    companion object {

        internal const val SECRET_ONE = "test/value/one"
        internal const val SECRET_TWO = "/test/value/two"

        internal val parameterValueOne = randomAlphanumeric(nextInt(5, 50))
        internal val parameterValueTwo = randomAlphanumeric(nextInt(5, 50))

        internal val secretKeyOne_1 = randomAlphanumeric(nextInt(5, 50))
        internal val secretKeyOne_2 = randomAlphanumeric(nextInt(5, 50))
        internal val secretValueOne_1 = randomAlphanumeric(nextInt(5, 50))
        internal val secretValueOne_2 = randomAlphanumeric(nextInt(5, 50))
        internal val secretKeyTwo = randomAlphanumeric(nextInt(5, 50))
        internal val secretValueTwo = randomAlphanumeric(nextInt(5, 50))

        internal val standard: String = randomAlphanumeric(nextInt(5, 50))
    }
}
