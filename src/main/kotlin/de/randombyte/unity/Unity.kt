package de.randombyte.unity

import com.google.inject.Inject
import de.randombyte.kosp.bstats.BStats
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.toOptional
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Unity.Companion.AUTHOR
import de.randombyte.unity.Unity.Companion.ID
import de.randombyte.unity.Unity.Companion.NAME
import de.randombyte.unity.Unity.Companion.NUCLEUS_ID
import de.randombyte.unity.Unity.Companion.VERSION
import de.randombyte.unity.commands.*
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments.player
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.filter.Getter
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStartingServerEvent
import org.spongepowered.api.event.service.ChangeServiceProviderEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import java.util.*

@Plugin(id = ID,
        name = NAME,
        version = VERSION,
        authors = arrayOf(AUTHOR),
        dependencies = arrayOf(Dependency(id = NUCLEUS_ID, optional = true)))
class Unity @Inject constructor(
        val logger: Logger,
        @DefaultConfig(sharedRoot = true) configurationLoader: ConfigurationLoader<CommentedConfigurationNode>,
        val bStats: BStats,
        val pluginContainer: PluginContainer
) {
    companion object {
        const val ID = "unity"
        const val NAME = "Unity"
        const val VERSION = "1.0"
        const val AUTHOR = "RandomByte"

        const val NUCLEUS_ID = "nucleus"

        const val ROOT_PERMISSION = ID
        const val PLAYER_PERMISSION = "$ROOT_PERMISSION.player"

        const val PLAYER_ARG = "player"
    }

    private val configManager = ConfigManager(
            configLoader = configurationLoader,
            clazz = Config::class.java,
            hyphenSeparatedKeys = true,
            simpleTextSerialization = true,
            simpleTextTemplateSerialization = true
    )

    // <requestee, requesters>
    private val unityRequests: MutableMap<UUID, List<UUID>> = mutableMapOf()

    @Listener
    fun onInit(event: GameInitializationEvent) {
        registerCommands()

        logger.info("Loaded $NAME: $VERSION")
    }

    @Listener
    fun onWorldsLoaded(event: GameStartingServerEvent) {
        // do this here to ensure all worlds are loaded for location deserialization
        configManager.generate()
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        configManager.generate()
        unityRequests.clear()

        logger.info("Reloaded!")
    }

    @Listener
    fun onChangeServiceProvider(event: ChangeServiceProviderEvent, @Getter("getNewProvider") messageTokenService: NucleusMessageTokenService) {
        messageTokenService.register(pluginContainer, NucleusMessageTokenService.TokenParser { tokenInput, source, _ ->
            if (tokenInput != "marry" || source !is Player) return@TokenParser Optional.empty()

            val config = configManager.get()
            if (config.unities.getUnity(source.uniqueId) == null) {
                return@TokenParser Optional.empty()
            }

            return@TokenParser config.marriedPrefix.toOptional()
        })
    }

    private fun registerCommands() {
        val removeRequest = { requester: UUID, requestee: UUID ->
            unityRequests += (requestee to (unityRequests[requestee] ?: emptyList()).filterNot { it == requester })
        }

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                // the root unity/marry command is the 'request unity'-command
                .permission(PLAYER_PERMISSION)
                .arguments(player(PLAYER_ARG.toText()))
                .executor(RequestUnityCommand(
                        configManager,
                        addRequest = { requester, requestee ->
                            val exisitingRequesters = unityRequests[requestee] ?: emptyList()
                            if (requester in exisitingRequesters) return@RequestUnityCommand false
                            unityRequests += (requestee to (exisitingRequesters + requester))
                            return@RequestUnityCommand true
                        }
                ))

                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(HelpCommand(configManager))
                        .build(), "help")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .arguments(player(PLAYER_ARG.toText()))
                        .executor(AcceptRequestCommand(configManager, this::unityRequests, removeRequest))
                        .build(), "accept")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .arguments(player(PLAYER_ARG.toText()))
                        .executor(DeclineRequestCommand(configManager, this::unityRequests, removeRequest))
                        .build(), "decline")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .arguments(player(PLAYER_ARG.toText()))
                        .executor(CancelRequestCommand(configManager, this::unityRequests, removeRequest))
                        .build(), "cancel")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(ListUnitiesCommand(configManager))
                        .build(), "list")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(DivorceCommand(configManager))
                        .build(), "divorce")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(TeleportCommand(configManager))
                        .build(), "teleport", "tp")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(GiftCommand(configManager, Cause.source(this).build()))
                        .build(), "gift")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(HomeCommand(configManager))
                        .child(CommandSpec.builder()
                                .permission(PLAYER_PERMISSION)
                                .executor(SetHomeCommand(configManager))
                                .build(), "set")
                        .build(), "home")
                .build(), "unity", "marry")
    }
}