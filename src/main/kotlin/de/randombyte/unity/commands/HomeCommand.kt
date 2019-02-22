package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.config.OldConfig
import de.randombyte.unity.config.OldConfig.Unity.HomeLocation.*
import de.randombyte.unity.config.OldConfig.Unity.HomeLocation.Set
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class HomeCommand() : UnityCommand(configAccessor) {
    override fun executedByUnityMember(player: Player, args: CommandContext, thisUnity: OldConfig.Unity, config: OldConfig): CommandResult {
        val home = thisUnity.tryGetHomeLocation()
        when (home) {
            NotSet -> throw CommandException("No home set!".toText())
            Unreachable -> throw CommandException("The home is not reachable! The world is not loaded.".toText())
            is Set -> player.location = home.location
        }

        return CommandResult.success()
    }
}