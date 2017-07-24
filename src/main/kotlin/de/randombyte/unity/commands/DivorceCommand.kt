package de.randombyte.unity.commands

import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.getUser
import de.randombyte.unity.Config
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class DivorceCommand(
        val configManager: ConfigManager<Config>
) : UnityCommand(getConfig = configManager::get) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val newConfig = config.copy(unities = config.unities - thisUnity)
        configManager.save(newConfig)

        val otherMember = with(thisUnity) {
            when {
                thisUnity.member1 == player.uniqueId -> thisUnity.member2
                thisUnity.member2 == player.uniqueId -> thisUnity.member1
                else -> throw RuntimeException("Getting other member failed, report to developer!")
            }
        }

        val otherMemberName = otherMember.getUser()?.name ?: "Unknown"

        val divorceBroadcast = config.texts.divorceBroadcast.apply(mapOf(
                "member1" to player.name,
                "member2" to otherMemberName)).build()
        broadcast(divorceBroadcast)

        return CommandResult.success()
    }
}