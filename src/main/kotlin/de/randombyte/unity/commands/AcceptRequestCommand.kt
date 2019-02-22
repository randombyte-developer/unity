package de.randombyte.unity.commands

import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.extensions.deserialize
import de.randombyte.kosp.extensions.replace
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Unity
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class AcceptRequestCommand : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val requester = args.getOne<Player>(Unity.PLAYER_ARG).get()

        val requests = Unity.INSTANCE.requests
        if (!requests.hasRequest(requestee = player.uniqueId, requester = requester.uniqueId)) {
            throw CommandException("You don't have a request from '${requester.name}'!".toText())
        }

        val databaseConfigHolder = Unity.INSTANCE.configAccessor.unitiesDatabase
        val databaseConfig = databaseConfigHolder.get()
        val textsConfig = Unity.INSTANCE.configAccessor.texts.get()

        databaseConfig.requireSingle(self = player, other = requester)

        requests.removeRequest(requestee = player.uniqueId, requester = requester.uniqueId)
        val newConfig = databaseConfig.newUnity(player.uniqueId, requester.uniqueId)
        databaseConfigHolder.save(newConfig)

        broadcastIfNotEmpty(textsConfig.unityBroadcast.replace(
                "member1" to requester.name,
                "member2" to player.name
        ).deserialize())

        return CommandResult.success()
    }
}