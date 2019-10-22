This kotlin library allows an easy integration for AWS Secretsmanager in your Spring Boot application.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.maccamlc/secrets-property-source/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.maccamlc/secrets-property-source)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Installation

Artifacts are published to maven central.

Gradle:

`compile com.github.maccamlc:secrets-property-source:0.2.0`

Maven:

```xml
<dependency>
    <groupId>com.github.maccamlc</groupId>
    <artifactId>secrets-property-source</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Usage

By including the dependency in your Spring Boot app, the library will be autoconfigured.

Secrets can be stored either as plain text or as JSON with multiple properties in AWS Secretsmanager.

For the property source to attempt resolving a property, the property name must start with **/secret/**.

The secrets are cached for one minute.

###  Plaintext

`${/secret/<secret-name>}`

*Example:*
`${/secret/my-service/plaintext-property}`

### JSON Property

`${/secret/<secret-name>.<json-property-name>}`

*Example:*

Let's assume we have a secret with the path *shipment-service/rds* that has two JSON properties: username and password.

`${/secret/shipment-service/rds.username}`

`${/secret/shipment-service/rds.password}`

An example configuration of your application yaml might look like this:

```
spring:
  datasource:
    url: jdbc:postgresql://host:5432/shipment_service # host could retreived from secretsmanager aswell
    username: ${/secret/shipment-service/rds.username}
    password: ${/secret/shipment-service/rds.password}
    platform: POSTGRESQL
```