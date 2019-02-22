package de.randombyte.unity

import com.flowpowered.math.vector.Vector3d
import com.google.inject.Inject
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.config.serializers.date.SimpleDateTypeSerializer
import de.randombyte.kosp.extensions.*
import de.randombyte.unity.Unity.Companion.AUTHOR
import de.randombyte.unity.Unity.Companion.ID
import de.randombyte.unity.Unity.Companion.NAME
import de.randombyte.unity.Unity.Companion.NUCLEUS_ID
import de.randombyte.unity.Unity.Companion.VERSION
import de.randombyte.unity.commands.*
import de.randombyte.unity.config.ConfigAccessor
import de.randombyte.unity.config.OldConfig
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.bstats.sponge.Metrics2
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments.player
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.data.property.entity.EyeLocationProperty
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.filter.Getter
import org.spongepowered.api.event.filter.cause.Root
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.service.ChangeServiceProviderEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.text.action.TextActions
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*

@Plugin(id = ID,
        name = NAME,
        version = VERSION,
        authors = [AUTHOR],
        dependencies = [(Dependency(id = NUCLEUS_ID, optional = true))])
class Unity @Inject constructor(
        private val logger: Logger,
        @DefaultConfig(sharedRoot = true) configurationLoader: ConfigurationLoader<CommentedConfigurationNode>,
        @ConfigDir(sharedRoot = false) configPath: Path,
        private val metrics: Metrics2,
        private val pluginContainer: PluginContainer
) {
    companion object {
        const val ID = "unity"
        const val NAME = "Unity"
        const val VERSION = "2.3.1"
        const val AUTHOR = "RandomByte"

        const val NUCLEUS_ID = "nucleus"

        const val ROOT_PERMISSION = ID
        const val PLAYER_PERMISSION = "$ROOT_PERMISSION.player"

        const val PLAYER_ARG = "player"

        val dateOutputFormat = SimpleDateFormat("dd.MM.yyyy")

        private val _INSTANCE = lazy { Sponge.getPluginManager().getPlugin(ID).get().instance.get() as Unity }
        val INSTANCE: Unity get() = _INSTANCE.value
    }

    private val oldConfigManager = ConfigManager(
            configLoader = configurationLoader,
            clazz = OldConfig::class.java
    )

    val configAccessor = ConfigAccessor(configPath)

    val requests: RequestsManager = RequestsManager()

    private val kissingParticleEffect = lazy {
        ParticleEffect.builder()
                .type(ParticleTypes.HEART)
                .quantity(1)
                .offset(Vector3d(0.3, 0.3, 0.3))
                .velocity(Vector3d(0.1, 0.1, 0.0))
                .build()
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        configAccessor.reloadAll()
        registerCommands()

        logger.info("Loaded $NAME: $VERSION")
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        configAccessor.reloadAll()
        requests.clear()

        logger.info("Reloaded!")
    }

    @Listener
    fun onChangeServiceProvider(event: ChangeServiceProviderEvent) {
        if (event.service.name != "io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService") return
        (event.newProvider as NucleusMessageTokenService).register(pluginContainer,
                NucleusMessageTokenService.TokenParser { tokenInput, source, _ ->
                    if (tokenInput != "marry" || source !is Player) return@TokenParser Optional.empty()

                    val config = oldConfigManager.get()
                    val unity = config.unities.getUnity(source.uniqueId) ?: return@TokenParser Optional.empty()

                    val otherMemberName = unity.getOtherMember(source.uniqueId).getUser()?.name ?: "Unknown"
                    SimpleDateTypeSerializer
                    val hoverText = "Married to ".toText() + otherMemberName.gold() + ", " + dateOutputFormat.format(unity.date)

                    return@TokenParser config.marriedPrefix.action(TextActions.showText(hoverText)).toOptional()
        })
    }

    @Listener
    fun onKissPartner(event: InteractEntityEvent.Secondary.MainHand, @Root player: Player, @Getter("getTargetEntity") partner: Player) {
        if (!configAccessor.general.get().kissingEnabled || !player.get(Keys.IS_SNEAKING).orElse(false)) return
        with (configAccessor.unitiesDatabase.get().unities) {
            val unity = getUnity(player.uniqueId) ?: return
            if (unity.getOtherMember(player.uniqueId) != partner.uniqueId) return
            partner.world.spawnParticles(kissingParticleEffect.value, partner.getProperty(EyeLocationProperty::class.java).get().value!!)
        }
    }

    private fun registerCommands() {
        val removeRequest = { requester: UUID, requestee: UUID ->
            requests += (requestee to (requests[requestee] ?: emptyList()).filterNot { it == requester })
        }

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                // the root unity/marry command is the 'request unity'-command
                .permission(PLAYER_PERMISSION)
                .arguments(player(PLAYER_ARG.toText()))
                .executor(RequestUnityCommand(
                        addRequest = { requester, requestee ->

                        }
                ))

                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(HelpCommand())
                        .build(), "help")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .arguments(player(PLAYER_ARG.toText()))
                        .executor(AcceptRequestCommand(this::requests, removeRequest))
                        .build(), "accept")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .arguments(player(PLAYER_ARG.toText()))
                        .executor(DeclineRequestCommand(this::requests, removeRequest))
                        .build(), "decline")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .arguments(player(PLAYER_ARG.toText()))
                        .executor(CancelRequestCommand(this::requests, removeRequest))
                        .build(), "cancel")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(ListUnitiesCommand())
                        .build(), "list")
                .child(CommandSpec.builder()
                        .permission(PLAYER_PERMISSION)
                        .executor(DivorceCommand())
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
                        .executor(HomeCommand())
                        .child(CommandSpec.builder()
                                .permission(PLAYER_PERMISSION)
                                .executor(SetHomeCommand())
                                .build(), "set")
                        .build(), "home")
                .build(), "unity", "marry")
    }
}