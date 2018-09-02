package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.getUser
import de.randombyte.kosp.extensions.give
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.config.Config
import de.randombyte.unity.config.ConfigAccessor
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.living.player.Player

class GiftCommand(
        configAccessor: ConfigAccessor
) : UnityCommand(configAccessor) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val itemInHand = player.getItemInHand(HandTypes.MAIN_HAND)
                .orElseThrow { CommandException("You must hold an item in your hand!".toText()) }

        val otherMember = thisUnity.getOtherMember(player.uniqueId).getUser()!!
        val otherPlayer = otherMember.player
                .orElseThrow { throw CommandException("'${otherMember.name}' must be online to execute this command!".toText()) }

        otherPlayer.give(itemInHand)
        player.setItemInHand(HandTypes.MAIN_HAND, null)

        otherPlayer.sendMessageIfNotEmpty(config.texts.receivedGift.apply(mapOf("otherMember" to player.name)).build())
        player.sendMessageIfNotEmpty(config.texts.sentGift.apply(mapOf("otherMember" to otherPlayer.name)).build())

        return CommandResult.success()
    }
}