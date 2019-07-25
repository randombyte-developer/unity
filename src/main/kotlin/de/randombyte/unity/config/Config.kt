package de.randombyte.unity.config

import de.randombyte.kosp.extensions.*
import de.randombyte.kosp.fixedTextTemplateOf
import de.randombyte.unity.Messages
import de.randombyte.unity.commands.sendMessageIfNotEmpty
import de.randombyte.unity.config.Config.Unity.HomeLocation.*
import de.randombyte.unity.config.Config.Unity.HomeLocation.Set
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.Sponge
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.TextTemplate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.time.Duration
import java.time.Instant
import java.util.*

@ConfigSerializable
data class Config(
        @Setting("unities") val unities: List<Unity> = emptyList(),
        @Setting("texts") val texts: Texts = Texts(),
        @Setting("divorce-cooldown") val divorceCooldown: Duration = Duration.ofDays(1),
        @Setting("married-prefix") val marriedPrefix: Text = "[".red() + "♥".darkRed() + "]".red(),
        @Setting("kissing-enabled", comment = "Shift-right-click your partner to spawn heart particles.") val kissingEnabled: Boolean = true,
        @Setting("enable-metrics-messages", comment = Messages.configComment) val enableMetricsMessages: Boolean = true
) {
    @ConfigSerializable
    data class Unity(
            @Setting("member1") val member1: UUID = UUID(0, 0),
            @Setting("member2") val member2: UUID = UUID(0, 0),
            @Setting("home") val home: ConfigLocation? = null,
            @Setting("date") val date: Date = Date.from(Instant.EPOCH)
    ) {

        // retro fits the Sponge Location serialization format, but we have control over when to
        // load and construct the Location
        @ConfigSerializable
        class ConfigLocation(
                @Setting("WorldUuid") val worldUuid: UUID = UUID(0, 0),
                @Setting("X") val x: Double = 0.0,
                @Setting("Y") val y: Double = 0.0,
                @Setting("Z") val z: Double = 0.0
        ) {
            constructor(location: Location<World>) : this(location.extent.uniqueId, location.x, location.y, location.z)
        }

        fun sendMessage(text: Text) {
            member1.getPlayer()?.sendMessageIfNotEmpty(text)
            member2.getPlayer()?.sendMessageIfNotEmpty(text)
        }

        fun getOtherMember(member: UUID) = when {
            member1 == member -> member2
            member2 == member -> member1
            else -> throw RuntimeException("Getting other member failed, report to developer!")
        }

        sealed class HomeLocation {
            object NotSet : HomeLocation()
            object Unreachable : HomeLocation()
            class Set(val location: Location<World>) : HomeLocation()
        }

        fun tryGetHomeLocation(): HomeLocation {
            if (home == null) return NotSet
            val world = Sponge.getServer().getWorld(home.worldUuid).orNull() ?: return Unreachable
            return Set(world.getLocation(home.x, home.y, home.z))
        }
    }

    @ConfigSerializable
    class Texts(
            @Setting("sent-request") val sentRequest: TextTemplate = fixedTextTemplateOf(
                    "Proposal sent to ".aqua(), "requestee".toArg().white(), "!".aqua()),

            @Setting("got-request") val gotRequest: TextTemplate = fixedTextTemplateOf(
                    "requester".toArg().white(), " wants to marry you!".aqua()),
            @Setting("accept-request-action") val acceptRequestAction: Text = "[ACCEPT]".green(),
            @Setting("decline-request-action") val declineRequestAction: Text = "[DECLINE]".red(),

            @Setting("cancelled-request-message") val cancelledRequestMessage: TextTemplate = fixedTextTemplateOf(
                    "requester".toArg().white(), " cancelled the request!".aqua()
            ),

            @Setting("sent-cancellation") val sentCancellation: TextTemplate = fixedTextTemplateOf(
                    "Sent cancellation to ".aqua(), "requestee".toArg().white(), "!".aqua()
            ),

            @Setting("request-broadcast") val requestBroadcast: TextTemplate = fixedTextTemplateOf(
                    "requester".toArg().white(), " sent a proposal to ".aqua(), "requestee".toArg().white(), "!".aqua()),

            @Setting("unity-broadcast") val unityBroadcast: TextTemplate = fixedTextTemplateOf(
                    "member1".toArg().white(), " and ".aqua(), "member2".toArg().white(), " just got married!".aqua()),

            @Setting("declined-request-broadcast") val declinedRequestBroadcast: TextTemplate = fixedTextTemplateOf(
                    "requestee".toArg().white(), " declined to marry ".aqua(), "requester".toArg().white(), "!".aqua()),

            @Setting("divorce-broadcast") val divorceBroadcast: TextTemplate = fixedTextTemplateOf(
                    "member1".toArg().white(), " just divorced from ".aqua(), "member2".toArg().white(), "!".aqua()),

            @Setting("list-command-title") val listCommandTitle: Text = "Marriages".yellow(),
            @Setting("list-command-entry") val listCommandEntry: TextTemplate = fixedTextTemplateOf(
                    "- ".aqua(), "member1".toArg().white(), " is married to ".aqua(), "member2".toArg().white()),

            @Setting("sent-gift") val sentGift: TextTemplate = fixedTextTemplateOf(
                    "Sent gift to ".aqua(), "otherMember".toArg().white(), "!".aqua()
            ),
            @Setting("received-gift") val receivedGift: TextTemplate = fixedTextTemplateOf(
                    "Received gift from ".aqua(), "otherMember".toArg().white(), "!".aqua()
            ),

            @Setting("help-command-title") val helpCommandTitle: Text = "Marriages".yellow(),
            @Setting("help-command-entries") val helpCommandEntries: List<Text> = listOf(
                    "- /marry help - Shows this page".toText(),
                    "- /marry <name> - Marry a player".toText(),
                    "- /marry accept - Accept the proposal".toText(),
                    "- /marry decline - Decline the proposal".toText(),
                    "- /marry list - List all couples".toText(),
                    "- /marry divorce - Divorce from your partner".toText(),
                    "- /marry tp - Teleport to your partner".toText(),
                    "- /marry gift - Gift the item you are holding to your partner".toText(),
                    "- /marry home - Teleport to the couple's home".toText(),
                    "- /marry home set - Set the couple's home".toText()
            )
    )
}