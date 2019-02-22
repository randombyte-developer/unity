package de.randombyte.unity.commands

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import java.util.*

fun broadcastIfNotEmpty(text: Text) {
    if (!text.isEmpty) Sponge.getServer().broadcastChannel.send(text)
}

fun Player.sendMessageIfNotEmpty(text: Text) {
    if (!text.isEmpty) this.sendMessage(text)
}