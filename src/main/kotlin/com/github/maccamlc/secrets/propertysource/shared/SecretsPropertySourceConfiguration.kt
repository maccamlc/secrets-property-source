package com.github.maccamlc.secrets.propertysource.shared

import java.time.Duration

object SecretsPropertySourceConfiguration {

    var awsSecretsManagerPropertySourceEnabled: Boolean = true

    var awsParameterStorePropertySourceEnabled: Boolean = true

    /**
     * Configure how the Cache is used for retrieving external secrets
     *
     * Specify a [Duration] to use for cache expiry after write, with default of 1 minute. Setting to null will disable caching
     */
    var cacheExpiry: Duration? = Duration.ofMinutes(1)
}
