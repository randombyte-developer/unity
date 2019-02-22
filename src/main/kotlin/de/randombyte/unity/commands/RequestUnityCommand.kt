package de.randombyte.unity.commands

import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.extensions.*
import de.randombyte.unity.Unity
import de.randombyte.unity.config.TextsConfig.Placeholders
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.action.TextActions

class RequestUnityCommand : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val requestee = args.getOne<Player>(Unity.PLAYER_ARG).get()

        val databaseConfig = Unity.INSTANCE.configAccessor.unitiesDatabase.get()
        val textsConfig = Unity.INSTANCE.configAccessor.texts.get()

        databaseConfig.requireSingle(self = player, other = requestee)

        if (requestee.uniqueId == player.uniqueId) throw CommandException("Choose another player!".toText())

        val requests = Unity.INSTANCE.requests

        if (requests.hasRequest(requestee = requestee.uniqueId, requester = player.uniqueId)) {
            throw CommandException("You already sent a request to '${requestee.name}'!".toText())
        }
        requests.addRequest(requestee = requestee.uniqueId, requester = player.uniqueId)

        val acceptRequest = textsConfig.acceptRequestAction.deserialize()
                .action(TextActions.runCommand("/unity accept ${player.name}"))
        val declineRequest = textsConfig.declineRequestAction.deserialize()
                .action(TextActions.runCommand("/unity decline ${player.name}"))
        val requestMessage = textsConfig.gotRequest.replace(Placeholders.REQUESTER to player.name).deserialize()
        requestee.sendMessageIfNotEmpty(requestMessage + " " + acceptRequest + " " + declineRequest)

        player.sendMessageIfNotEmpty(textsConfig.sentRequest.replace(Placeholders.REQUESTEE to requestee.name).deserialize())

        broadcastIfNotEmpty(textsConfig.requestBroadcast.replace(
                "requester" to player.name,
                "requestee" to requestee.name
        ).deserialize())

        return CommandResult.success()
    }
}