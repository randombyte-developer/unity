package de.randombyte.unity.commands.commandelement

import de.randombyte.kosp.extensions.getPlayer
import de.randombyte.kosp.extensions.toText
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.CommandElement
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import java.util.*

class UnityRequesters(text: Text, val getRequestsToPlayer: (Player) -> List<UUID>) : CommandElement(text) {
    override fun parseValue(source: CommandSource, args: CommandArgs): Player {
        source.checkPlayer(args)

        val playerName = args.next()
        val argPlayer = Sponge.getServer().onlinePlayers.firstOrNull { it.name == playerName }
                ?: throw args.createError("Couldn't find player '$playerName'!".toText())

        val requestsToPlayer = getRequestsToPlayer(source as Player) // safe because of 'checkPlayer()'
        if (argPlayer.uniqueId !in requestsToPlayer) throw args.createError("You don't have a request from '$playerName'!".toText())

        return argPlayer
    }

    override fun complete(source: CommandSource, args: CommandArgs, context: CommandContext): MutableList<String> {
        source.checkPlayer(args)

        val prefix = args.nextIfPresent().orElse("")
        return getRequestsToPlayer(source as Player)
                .mapNotNull(UUID::getPlayer)
                .filter { it.name.startsWith(prefix) }
                .map(Player::getName)
                .toMutableList()
    }

    private fun CommandSource.checkPlayer(args: CommandArgs) {
        if (this !is Player) throw args.createError("This command must be executed by a player!".toText())
    }
}