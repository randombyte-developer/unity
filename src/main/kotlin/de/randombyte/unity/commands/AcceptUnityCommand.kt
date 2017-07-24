package de.randombyte.unity.commands

import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.unity.Config
import de.randombyte.unity.Unity
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import java.util.*

class AcceptUnityCommand(
        val configManager: ConfigManager<Config>,
        val removeRequest: (requester: UUID, requestee: UUID) -> Unit
) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val requester = args.getOne<Player>(Unity.PLAYER_ARG).get()

        val config = configManager.get()
        requireSingle(player, requester, config.unities)

        removeRequest(requester.uniqueId, player.uniqueId)
        val newConfig = config.copy(unities = config.unities + Config.Unity(
                member1 = requester.uniqueId,
                member2 = player.uniqueId
        ))
        configManager.save(newConfig)

        val broadcastMessage = config.texts.unityBroadcast.apply(mapOf(
                "member1" to requester.name,
                "member2" to player.name)).build()
        broadcast(broadcastMessage)

        return CommandResult.success()
    }
}