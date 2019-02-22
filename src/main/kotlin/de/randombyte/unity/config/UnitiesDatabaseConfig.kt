package de.randombyte.unity.config

import de.randombyte.kosp.extensions.getPlayer
import de.randombyte.kosp.extensions.orNull
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.commands.sendMessageIfNotEmpty
import de.randombyte.unity.config.UnitiesDatabaseConfig.Unity.HomeLocation.*
import de.randombyte.unity.config.UnitiesDatabaseConfig.Unity.HomeLocation.Set
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.time.Instant
import java.util.*

@ConfigSerializable
data class UnitiesDatabaseConfig(
        @Setting("unities") val unities: List<Unity> = emptyList()
) {

    @ConfigSerializable
    data class Unity(
            @Setting("member1") val member1: UUID = UUID(0, 0),
            @Setting("member2") val member2: UUID = UUID(0, 0),
            @Setting("home") val home: ConfigLocation? = null,
            @Setting("date") val date: Date = Date.from(Instant.EPOCH)
    ) {

        @ConfigSerializable
        class ConfigLocation(
                @Setting("world-uuid") val worldUuid: UUID = UUID(0, 0),
                @Setting("x") val x: Double = 0.0,
                @Setting("y") val y: Double = 0.0,
                @Setting("z") val z: Double = 0.0
        ) {
            constructor(location: Location<World>) : this(location.extent.uniqueId, location.x, location.y, location.z)
        }

        fun sendMessage(text: Text) {
            member1.getPlayer()?.sendMessageIfNotEmpty(text)
            member2.getPlayer()?.sendMessageIfNotEmpty(text)
        }

        fun getOtherMember(member: UUID) = when {
            member1 == member -> member2
            member2 == member -> member1
            else -> throw RuntimeException("Getting other member failed, report to developer!")
        }

        sealed class HomeLocation {
            object NotSet : HomeLocation()
            object NotLoaded : HomeLocation()
            class Set(val location: Location<World>) : HomeLocation()
        }

        fun tryGetHomeLocation(): HomeLocation {
            if (home == null) return NotSet
            val world = Sponge.getServer().getWorld(home.worldUuid).orNull() ?: return NotLoaded
            return Set(world.getLocation(home.x, home.y, home.z))
        }
    }

    fun newUnity(member1: UUID, member2: UUID) = copy(
            unities = unities + Unity(
            member1 = member1,
            member2 = member2,
            date = Date.from(Instant.now())
    ))

    fun getUnity(playerUuid: UUID) = unities.firstOrNull { (member1, member2) -> member1 == playerUuid || member2 == playerUuid }
    fun isSingle(playerUuid: UUID) = getUnity(playerUuid) == null

    fun requireSingle(self: Player, other: Player) {
        if (!isSingle(self.uniqueId)) throw CommandException("You must be single to execute this command!".toText())
        if (!isSingle(other.uniqueId)) throw CommandException("'${other.name}' must be single to execute this command!".toText())
    }
}