package de.randombyte.unity.commands

import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Unity
import de.randombyte.unity.config.ConfigAccessor
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import java.util.*

class CancelRequestCommand(
        val configAccessor: ConfigAccessor,
        val getRequests: () -> Map<UUID, List<UUID>>,
        val removeRequest: (requester: UUID, requestee: UUID) -> Unit
) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val requestee = args.getOne<Player>(Unity.PLAYER_ARG).get()
        if (!player.checkRequester(requestee = requestee, requests = getRequests())) {
            throw CommandException("You don't have a request to '${requestee.name}'!".toText())
        }

        removeRequest(player.uniqueId, requestee.uniqueId)

        val config = configAccessor.get()
        requestee.sendMessageIfNotEmpty(config.texts.cancelledRequestMessage.apply(mapOf(
                "requester" to player.name
        )).build())

        player.sendMessageIfNotEmpty(config.texts.sentCancellation.apply(mapOf(
                "requestee" to requestee.name
        )).build())

        return CommandResult.success()
    }
}