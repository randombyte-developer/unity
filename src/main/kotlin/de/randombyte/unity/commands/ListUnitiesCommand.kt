package de.randombyte.unity.commands

import de.randombyte.kosp.extensions.getUser
import de.randombyte.kosp.getServiceOrFail
import de.randombyte.unity.config.ConfigAccessor
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.service.pagination.PaginationService

class ListUnitiesCommand(
        val configAccessor: ConfigAccessor
) : CommandExecutor {
    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val config = configAccessor.get()

        val contents = config.unities.map { unity ->
            config.texts.listCommandEntry.apply(mapOf(
                    "member1" to (unity.member1.getUser()?.name ?: "Unknown"),
                    "member2" to (unity.member2.getUser()?.name ?: "Unknown")
            )).build()
        }

        getServiceOrFail(PaginationService::class).builder()
                .title(config.texts.listCommandTitle)
                .contents(contents)
                .sendTo(src)

        return CommandResult.success()
    }
}