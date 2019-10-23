This kotlin library allows an easy integration for AWS Secretsmanager and AWS Parameter Store (SSM) in your Spring Boot application.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.maccamlc/secrets-property-source/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.maccamlc/secrets-property-source)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This has been forked from [ottonow/aws-secrets-manager-property-source](https://github.com/ottonow/aws-secrets-manager-property-source) which contained the initial building blocks I was looking for, to solve my particular problem.

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

Alternatively, if there are stored in AWS Parameter Store (SSM) then they can be retrieved as plain text.

For the property source to attempt resolving a property, the property name must start with 

* AWS Secrets Manager: **/aws-secretsmanager/**
* AWS Parameter Store: **/aws-parameterstore/**

The secrets are cached for one minute.

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