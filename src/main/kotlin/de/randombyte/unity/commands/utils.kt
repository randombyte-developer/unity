package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.config.Config
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import java.util.*

fun Player.checkRequester(requestee: Player, requests: Map<UUID, List<UUID>>): Boolean {
    if (this == requestee) return false
    val requestsToPlayer = requests[requestee.uniqueId] ?: emptyList()
    return this.uniqueId in requestsToPlayer
}

fun requireSingle(commandSource: Player, otherPlayer: Player, unities: List<Config.Unity>) {
    with(unities) {
        if (!isSingle(commandSource.uniqueId)) throw CommandException("You must be single to execute this command!".toText())
        if (!isSingle(otherPlayer.uniqueId)) throw CommandException("'${otherPlayer.name}' must be single to execute this command!".toText())
    }
}

fun List<Config.Unity>.getUnity(playerUuid: UUID) = firstOrNull { (member1, member2) -> member1 == playerUuid || member2 == playerUuid }
fun List<Config.Unity>.isSingle(playerUuid: UUID) = getUnity(playerUuid) == null

fun broadcastIfNotEmpty(text: Text) {
    if (!text.isEmpty) Sponge.getServer().broadcastChannel.send(text)
}

fun Player.sendMessageIfNotEmpty(text: Text) {
    if (!text.isEmpty) this.sendMessage(text)
}