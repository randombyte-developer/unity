package de.randombyte.unity

import com.google.inject.Inject
import de.randombyte.kosp.bstats.BStats
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.toText
import de.randombyte.unity.Unity.Companion.AUTHOR
import de.randombyte.unity.Unity.Companion.ID
import de.randombyte.unity.Unity.Companion.NAME
import de.randombyte.unity.Unity.Companion.VERSION
import de.randombyte.unity.commands.*
import de.randombyte.unity.commands.commandelement.UnityRequesters
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments.player
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStartingServerEvent
import org.spongepowered.api.plugin.Plugin
import java.util.*

@Plugin(id = ID,
        name = NAME,
        version = VERSION,
        authors = arrayOf(AUTHOR))
class Unity @Inject constructor(
        val logger: Logger,
        @DefaultConfig(sharedRoot = true) configurationLoader: ConfigurationLoader<CommentedConfigurationNode>,
        val bStats: BStats
) {
    companion object {
        const val ID = "unity"
        const val NAME = "Unity"
        const val VERSION = "0.1"
        const val AUTHOR = "RandomByte"

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

        logger.info("Reloaded!")
    }

    private fun registerCommands() {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                // the root unity/marry command is the 'request unity'-command
                .permission(PLAYER_PERMISSION)
                .arguments(player(PLAYER_ARG.toText()))
                .executor(RequestUnityCommand(
                        configManager,
                        addRequest = { requester, requestee ->
                            unityRequests += (requestee to ((unityRequests[requestee] ?: emptyList()) + requester))
                        }
                ))

                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(RootHelpCommand())
                        .build(), "help")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .arguments(UnityRequesters(PLAYER_ARG.toText(),
                                getRequestsToPlayer = { player ->
                                    unityRequests[player.uniqueId] ?: emptyList()
                                }))
                        .executor(AcceptUnityCommand(
                                configManager,
                                removeRequest = { requester, requestee ->
                                    unityRequests += (requestee to (unityRequests[requestee] ?: emptyList()) - requester)
                                }
                        ))
                        .build(), "accept")
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
                        .executor(TeleportCommand())
                        .build(), "teleport", "tp")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(GiftCommand())
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