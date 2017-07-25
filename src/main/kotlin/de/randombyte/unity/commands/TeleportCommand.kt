package de.randombyte.unity.commands

import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.getUser
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Config
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class TeleportCommand(
        val configManager: ConfigManager<Config>
) : UnityCommand(getConfig = configManager::get) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val otherMember = thisUnity.getOtherMember(player.uniqueId).getUser()!!
        val otherPlayer = otherMember.player
                .orElseThrow { throw CommandException("'${otherMember.name}' must be online to excute this command!".toText()) }

        player.location = otherPlayer.location

        return CommandResult.success()
    }
}