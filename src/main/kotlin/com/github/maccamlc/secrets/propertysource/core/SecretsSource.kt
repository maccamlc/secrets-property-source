package com.github.maccamlc.secrets.propertysource.core

interface SecretsSource {

    fun getSecret(propertyName: String): String?
}
