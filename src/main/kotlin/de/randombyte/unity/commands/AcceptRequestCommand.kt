package de.randombyte.unity.commands

import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Unity
import de.randombyte.unity.config.Config
import de.randombyte.unity.config.ConfigAccessor
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import java.time.Instant
import java.util.*

class AcceptRequestCommand(
        val configAccessor: ConfigAccessor,
        val getRequests: () -> Map<UUID, List<UUID>>,
        val removeRequest: (requester: UUID, requestee: UUID) -> Unit
) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val requester = args.getOne<Player>(Unity.PLAYER_ARG).get()
        if (!requester.checkRequester(requestee = player, requests = getRequests())) {
            throw CommandException("You don't have a request from '${requester.name}'!".toText())
        }

        val config = configAccessor.get()
        requireSingle(player, requester, config.unities)

        removeRequest(requester.uniqueId, player.uniqueId)
        val newConfig = config.copy(unities = config.unities + Config.Unity(
                member1 = requester.uniqueId,
                member2 = player.uniqueId,
                date = Date.from(Instant.now())
        ))
        configAccessor.set(newConfig)

        val broadcastMessage = config.texts.unityBroadcast.apply(mapOf(
                "member1" to requester.name,
                "member2" to player.name)).build()
        broadcast(broadcastMessage)

        return CommandResult.success()
    }
}