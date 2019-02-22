package de.randombyte.unity.commands

import de.randombyte.kosp.getServiceOrFail
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.service.pagination.PaginationService

class HelpCommand() : CommandExecutor {
    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val config = configAccessor.get()

        getServiceOrFail(PaginationService::class).builder()
                .title(config.texts.helpCommandTitle)
                .contents(config.texts.helpCommandEntries)
                .sendTo(src)

        return CommandResult.success()
    }
}