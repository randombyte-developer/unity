package de.randombyte.unity.commands

import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.getUser
import de.randombyte.kosp.extensions.give
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Config
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause

class GiftCommand(
        val configManager: ConfigManager<Config>,
        val cause: Cause
) : UnityCommand(getConfig = configManager::get) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val itemInHand = player.getItemInHand(HandTypes.MAIN_HAND)
                .orElseThrow { CommandException("You must hold an item in your hand!".toText()) }

        val otherMember = thisUnity.getOtherMember(player.uniqueId).getUser()!!
        val otherPlayer = otherMember.player
                .orElseThrow { throw CommandException("'${otherMember.name}' must be online to excute this command!".toText()) }

        otherPlayer.give(itemInHand, cause)
        player.setItemInHand(HandTypes.MAIN_HAND, null)

        otherPlayer.sendMessage(config.texts.receivedGift.apply(mapOf("otherMember" to player.name)).build())
        player.sendMessage(config.texts.sentGift.apply(mapOf("otherMember" to player.name)).build())

        return CommandResult.success()
    }
}