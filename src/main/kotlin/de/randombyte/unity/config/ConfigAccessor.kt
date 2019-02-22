package de.randombyte.unity.config

import de.randombyte.kosp.config.ConfigAccessor
import java.nio.file.Path

class ConfigAccessor(configPath: Path) : ConfigAccessor(configPath) {

    val general = getConfigHolder<GeneralConfig>("general.conf")
    val unitiesDatabase = getConfigHolder<UnitiesDatabaseConfig>("unities.db")
    val texts = getConfigHolder<TextsConfig>("texts.conf")

    override val holders = listOf(general, unitiesDatabase, texts)
}