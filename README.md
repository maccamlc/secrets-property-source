This kotlin library allows an easy integration for AWS Secretsmanager and AWS Parameter Store (SSM) in your Spring Boot application.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.maccamlc/secrets-property-source/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.maccamlc/secrets-property-source)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This has been forked from [ottonow/aws-secrets-manager-property-source](https://github.com/ottonow/aws-secrets-manager-property-source) which contained the initial building blocks I was looking for, to solve my particular problem.

## Installation

Artifacts are published to maven central.

Gradle:

`implementation "com.github.maccamlc:secrets-property-source:0.3.0"`

Maven:

```xml
<dependency>
    <groupId>com.github.maccamlc</groupId>
    <artifactId>secrets-property-source</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Usage

By including the dependency in your Spring Boot app, the library will be autoconfigured.

Secrets can be stored either as plain text or as JSON with multiple properties in AWS Secretsmanager.

Alternatively, if there are stored in AWS Parameter Store (SSM) then they can be retrieved as plain text.

For the property source to attempt resolving a property, the property name must start with 

* AWS Secrets Manager: **/aws-secretsmanager/**
* AWS Parameter Store: **/aws-parameterstore/**

###  Plain text Secrets Manager

`${/aws-secretsmanager/<secret-name>}`

*Example:*
`${/aws-secretsmanager/my-service/plaintext-property}`

### Plain text Parameter Store

`${/aws-parameterstore/<secret-name>}`

*Example:*
`${/aws-parameterstore/my-service/plaintext-property}`

### JSON Property Secrets Manager

`${/aws-secretsmanager/<secret-name>.<json-property-name>}`

*Example:*

Let's assume we have a secret with the path *shipment-service/rds* that has two JSON properties: username and password.

```
{
  "username": "user1",
  "password": "password1"
}
```

Then they can be retrieved by:

* `${/aws-secretsmanager/shipment-service/rds.username}`
* `${/aws-secretsmanager/shipment-service/rds.password}`

or it can be retrieved as the complete JSON payload

* `${/aws-secretsmanager/shipment-service/rds}`

## Conclusion

An example configuration of your application yaml might look like this:

```
spring:
  datasource:
    url: ${/aws-secretsmanager/service/rds.endpoint}
    username: ${/aws-secretsmanager/service/rds.username}
    password: ${/aws-secretsmanager/service/rds.password}

service:
  auth:
    token: ${/aws-parameterstore//service/auth/token}

custom:
    json: ${/aws-secretsmanager/service/my-secrets}
```

## SecretsPropertySourceAccessor usage

Since the Property Sources can be loaded prior to bean initialization, by default new AWS clients will be created.

However, there are a couple options to avoid this:

* If retrieving properties programatically, such as `environment.getProperty(name)` then bean definitions will automatically
be autowired if available.

But for @Value resolution though, this approach will likely be too late. But you can manually set the values in SecretsPropertySourceAccessor
prior to starting your application, for example:
```
 companion object {

    init {
        SecretsPropertySourceAccessor.objectMapper = JacksonConfig.objectMapper
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runApplication<MyApplication>(*args)
    }
}
```

where JacksonConfig may look like:

```
@Configuration
class JacksonConfig {

    @Bean
    internal fun objectMapper(): ObjectMapper = objectMapper

    companion object {
        internal val objectMapper by lazy { jacksonObjectMapper().findAndRegisterModules() }
    }
}
```

This approach would allow to use customised AWS clients or ObjectMapper for loading your properties

## Caching

The secrets are cached for one minute by default, but can be configured (or disabled) before starting the application, for example:

```
 companion object {

    init {
        SecretsPropertySourceConfiguration.cacheExpiry = Duration.ofMinutes(5) // null would disable caching
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runApplication<MyApplication>(*args)
    }
}
```
