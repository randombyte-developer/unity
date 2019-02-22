package de.randombyte.unity.config

import de.randombyte.kosp.extensions.darkRed
import de.randombyte.kosp.extensions.plus
import de.randombyte.kosp.extensions.red
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.text.Text
import java.time.Duration
import java.time.Instant
import java.util.*

@ConfigSerializable
data class OldConfig(
        @Setting("unities") val unities: List<Unity> = emptyList(),
        @Setting("divorce-cooldown") val divorceCooldown: Duration = Duration.ofDays(1),
        @Setting("married-prefix") val marriedPrefix: Text = "[".red() + "♥".darkRed() + "]".red(),
        @Setting("kissing-enabled", comment = "Shift-right-click your partner to spawn heart particles.") val kissingEnabled: Boolean = true
) {
    @ConfigSerializable
    data class Unity(
            @Setting("member1") val member1: UUID = UUID(0, 0),
            @Setting("member2") val member2: UUID = UUID(0, 0),
            @Setting("home") val home: ConfigLocation? = null,
            @Setting("date") val date: Date = Date.from(Instant.EPOCH)
    ) {

        // retro fits the Sponge Location serialization format, but we have control over when to
        // load and construct the Location
        @ConfigSerializable
        class ConfigLocation(
                @Setting("WorldUuid") val worldUuid: UUID = UUID(0, 0),
                @Setting("X") val x: Double = 0.0,
                @Setting("Y") val y: Double = 0.0,
                @Setting("Z") val z: Double = 0.0
        )
    }
}