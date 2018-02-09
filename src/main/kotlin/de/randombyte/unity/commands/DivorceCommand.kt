package de.randombyte.unity.commands

import de.randombyte.kosp.config.serializers.duration.SimpleDurationTypeSerializer
import de.randombyte.kosp.extensions.getUser
import de.randombyte.kosp.extensions.red
import de.randombyte.unity.config.Config
import de.randombyte.unity.config.ConfigAccessor
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import java.time.Duration
import java.time.Instant

class DivorceCommand(
        configAccessor: ConfigAccessor
) : UnityCommand(configAccessor) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val firstAllowedDivorceDateMillis = thisUnity.date.toInstant().toEpochMilli().plus(config.divorceCooldown.toMillis())
        val remainingMillis = firstAllowedDivorceDateMillis - Instant.now().toEpochMilli()
        if (remainingMillis > 0) {
            val durationString = SimpleDurationTypeSerializer.serialize(Duration.ofMillis(remainingMillis), outputMilliseconds = false)
            throw CommandException("You can't divorce yet! Wait another $durationString.".red())
        }

        val newConfig = config.copy(unities = config.unities - thisUnity)
        configAccessor.set(newConfig)

        val otherMember = thisUnity.getOtherMember(player.uniqueId)
        val otherMemberName = otherMember.getUser()?.name ?: "Unknown"

        val divorceBroadcast = config.texts.divorceBroadcast.apply(mapOf(
                "member1" to player.name,
                "member2" to otherMemberName)).build()
        broadcast(divorceBroadcast)

        return CommandResult.success()
    }
}