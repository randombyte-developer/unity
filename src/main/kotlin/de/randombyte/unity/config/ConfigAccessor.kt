package de.randombyte.unity.config

abstract class ConfigAccessor {
    abstract fun get(): Config
    abstract fun set(config: Config)
}