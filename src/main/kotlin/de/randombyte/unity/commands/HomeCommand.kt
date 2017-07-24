package de.randombyte.unity.commands

import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Config
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class HomeCommand(
        configManager: ConfigManager<Config>
) : UnityCommand(getConfig = configManager::get) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val home = thisUnity.home ?: throw CommandException("No home set!".toText())
        player.location = home

        return CommandResult.success()
    }
}