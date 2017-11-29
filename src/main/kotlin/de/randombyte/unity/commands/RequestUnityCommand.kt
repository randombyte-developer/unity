package de.randombyte.unity.commands

import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.action
import de.randombyte.kosp.extensions.plus
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Config
import de.randombyte.unity.Unity
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.action.TextActions
import java.util.*

class RequestUnityCommand(
        val configManager: ConfigManager<Config>,
        val addRequest: (requester: UUID, requestee: UUID) -> Boolean
) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val requestee = args.getOne<Player>(Unity.PLAYER_ARG).get()

        val config = configManager.get()
        requireSingle(player, requestee, config.unities)

        if (requestee.uniqueId == player.uniqueId) throw CommandException("Choose another player!".toText())

        if (!addRequest(player.uniqueId, requestee.uniqueId)) {
            throw CommandException("You already sent a request to '${requestee.name}'!".toText())
        }

        val acceptRequest = config.texts.acceptRequestAction.action(TextActions.runCommand("/unity accept ${player.name}"))
        val declineRequest = config.texts.declineRequestAction.action(TextActions.runCommand("/unity decline ${player.name}"))
        val requestMessage = config.texts.gotRequest.apply(mapOf("requester" to player.name)).build()
        requestee.sendMessage(requestMessage + " " + acceptRequest + " " + declineRequest)

        player.sendMessage(config.texts.sentRequest.apply(mapOf("requestee" to requestee.name)).build())

        broadcast(config.texts.requestBroadcast.apply(mapOf(
                "requester" to player.name,
                "requestee" to requestee.name
        )).build())

        return CommandResult.success()
    }
}