package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.green
import de.randombyte.unity.config.Config
import de.randombyte.unity.config.ConfigAccessor
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class SetHomeCommand(
        configAccessor: ConfigAccessor
) : UnityCommand(configAccessor) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: Config.Unity, config: Config): CommandResult {
        val newHome = player.location
        configAccessor.set(config.copy(unities = (config.unities - thisUnity) + thisUnity.copy(home = newHome)))

        thisUnity.sendMessage("New home set".green())

        return CommandResult.success()
    }
}