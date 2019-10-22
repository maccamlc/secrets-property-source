package com.github.maccamlc.secrets.propertysource.core

import org.springframework.core.env.PropertySource

internal class SecretsPropertySource(
    name: String,
    source: SecretsSource,
    internal val prefix: String
) : PropertySource<SecretsSource>(name, source) {

    override fun getProperty(propertyName: String): Any? =
        if (propertyName.startsWith(prefix)) {
            source.getSecret(propertyName.replace(prefix, ""))
        } else null
}
