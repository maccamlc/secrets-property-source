package com.github.maccamlc.secrets.propertysource.shared

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
internal open class PropertySourceAccessor {

    @PostConstruct
    private fun postConstruct() = REFERENCE.set(this)

    @Autowired(required = false)
    private var awsSecretManager: AWSSecretsManager? = null

    @Autowired(required = false)
    private var awsSimpleSystemsManagement: AWSSimpleSystemsManagement? = null

    @Autowired(required = false)
    private var objectMapper: ObjectMapper? = null

    companion object {
        private val REFERENCE: AtomicReference<PropertySourceAccessor> = AtomicReference()

        internal fun getAwsSecretsManager(): AWSSecretsManager? =
            REFERENCE.get()?.awsSecretManager

        internal fun getAwsSimpleSystemsManagement(): AWSSimpleSystemsManagement? =
            REFERENCE.get()?.awsSimpleSystemsManagement

        internal fun getObjectMapper(): ObjectMapper? =
            REFERENCE.get()?.objectMapper
    }
}
