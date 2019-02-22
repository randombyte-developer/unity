package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.getUser
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.config.OldConfig
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class TeleportCommand() : UnityCommand(configAccessor) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: OldConfig.Unity, config: OldConfig): CommandResult {
        val otherMember = thisUnity.getOtherMember(player.uniqueId).getUser()!!
        val otherPlayer = otherMember.player
                .orElseThrow { throw CommandException("'${otherMember.name}' must be online to execute this command!".toText()) }

        player.location = otherPlayer.location

        return CommandResult.success()
    }
}