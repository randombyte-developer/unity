package de.randombyte.unity.config

import de.randombyte.kosp.extensions.darkRed
import de.randombyte.kosp.extensions.plus
import de.randombyte.kosp.extensions.red
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.text.Text
import java.time.Duration

@ConfigSerializable
class GeneralConfig (
        @Setting("divorce-cooldown") val divorceCooldown: Duration = Duration.ofDays(1),
        @Setting("married-prefix") val marriedPrefix: Text = "[".red() + "♥".darkRed() + "]".red(),
        @Setting("kissing-enabled", comment = "Shift-right-click your partner to spawn heart particles.") val kissingEnabled: Boolean = true
)