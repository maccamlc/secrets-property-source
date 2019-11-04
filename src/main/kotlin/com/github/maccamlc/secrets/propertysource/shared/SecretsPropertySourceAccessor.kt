package com.github.maccamlc.secrets.propertysource.shared

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
open class SecretsPropertySourceAccessor {

    @Autowired(required = false)
    private var awsSecretManager: AWSSecretsManager? = null

    @Autowired(required = false)
    private var awsSimpleSystemsManagement: AWSSimpleSystemsManagement? = null

    @Autowired(required = false)
    private var objectMapper: ObjectMapper? = null

    @PostConstruct
    private fun postConstruct() = REFERENCE.set(this)

    @PreDestroy
    private fun preDestroy() = REFERENCE.set(null)

    companion object {
        private val REFERENCE: AtomicReference<SecretsPropertySourceAccessor> = AtomicReference()
        private val CUSTOM_MAP = mutableMapOf<String, Any>()

        var awsSecretsManager: AWSSecretsManager?
            set(value) {
                CUSTOM_MAP.compute("awsSecretsManager") { _, _ -> value }
            }
            get() = CUSTOM_MAP.getOrElse("awsSecretsManager") {
                REFERENCE.get()?.awsSecretManager
            } as? AWSSecretsManager

        var awsSimpleSystemsManagement: AWSSimpleSystemsManagement?
            set(value) {
                CUSTOM_MAP.compute("awsSimpleSystemsManagement") { _, _ -> value }
            }
            get() = CUSTOM_MAP.getOrElse("awsSimpleSystemsManagement") {
                REFERENCE.get()?.awsSimpleSystemsManagement
            } as? AWSSimpleSystemsManagement

        var objectMapper: ObjectMapper?
            set(value) {
                CUSTOM_MAP.compute("objectMapper") { _, _ -> value }
            }
            get() = CUSTOM_MAP.getOrElse("objectMapper") { REFERENCE.get()?.objectMapper } as? ObjectMapper
    }
}
