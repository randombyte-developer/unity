package de.randombyte.unity.commands

import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.green
import de.randombyte.unity.Config
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class SetHomeCommand(
        val configManager: ConfigManager<Config>
) : UnityCommand(getConfig = configManager::get) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val newHome = player.location
        configManager.save(config.copy(unities = (config.unities - thisUnity) + thisUnity.copy(home = newHome)))

        thisUnity.sendMessage("New home set".green())

        return CommandResult.success()
    }
}