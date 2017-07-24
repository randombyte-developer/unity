package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Config
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import java.util.*

fun requireSingle(commandSource: Player, otherPlayer: Player, unities: List<Config.Unity>) {
    with(unities) {
        if (!isSingle(commandSource.uniqueId)) throw CommandException("You must be single to execute this command!".toText())
        if (!isSingle(otherPlayer.uniqueId)) throw CommandException("'${otherPlayer.name}' must be single to execute this command!".toText())
    }
}

fun List<Config.Unity>.getUnity(playerUuid: UUID) = firstOrNull { (member1, member2) -> member1 == playerUuid || member2 == playerUuid }
fun List<Config.Unity>.isSingle(playerUuid: UUID) = getUnity(playerUuid) == null

fun broadcast(text: Text) = Sponge.getServer().broadcastChannel.send(text)