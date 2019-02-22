package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.green
import de.randombyte.unity.config.OldConfig
import de.randombyte.unity.config.OldConfig.Unity.ConfigLocation
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class SetHomeCommand() : UnityCommand(configAccessor) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: OldConfig.Unity, config: OldConfig): CommandResult {
        val newHome = player.location
        configAccessor.set(config.copy(
                unities = (config.unities - thisUnity) + thisUnity.copy(home = ConfigLocation(newHome))))

        thisUnity.sendMessage("New home set".green())

        return CommandResult.success()
    }
}