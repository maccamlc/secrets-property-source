package com.github.maccamlc.secrets.propertysource.aws.localstack

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service

internal class SpringBootLocalstackTestContainerInitializer :
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        LOCALSTACK.start()

        configurableApplicationContext.beanFactory.registerSingleton("localStackContainer", LOCALSTACK)

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            configurableApplicationContext,
            "param.store.value1=\${/aws-parameterstore/test/value/one}",
            "param.store.value2=\${/aws-parameterstore//test/value/two}",
            "param.secret.value1=\${/aws-secretsmanager/test/value/one}",
            "param.secret.value1.key1=\${/aws-secretsmanager/test/value/one.${SpringBootLocalstackTestConfiguration.secretKeyOne_1}}",
            "param.secret.value2=\${/aws-secretsmanager//test/value/two}",
            "param.secret.value2.key=\${/aws-secretsmanager//test/value/two.${SpringBootLocalstackTestConfiguration.secretKeyTwo}}",
            "standard.value=${SpringBootLocalstackTestConfiguration.standard}"
        )
    }

    companion object {

        private const val LOCALSTACK_IMAGE_TAG = "0.10.4"

        private val LOCALSTACK =
            LocalStackContainer(LOCALSTACK_IMAGE_TAG).withServices(Service.SSM, Service.SECRETSMANAGER)
    }
}
