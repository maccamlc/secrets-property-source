package com.github.maccamlc.secrets.propertysource.aws.localstack

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(
    classes = [TestApplication::class],
    initializers = [SpringBootLocalstackTestContainerInitializer::class]
)
annotation class SpringBootLocalstackIntegrationTest
