package de.randombyte.unity

import de.randombyte.kosp.extensions.*
import de.randombyte.kosp.fixedTextTemplateOf
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.TextTemplate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

@ConfigSerializable
data class Config(
        @Setting val unities: List<Unity> = emptyList(),
        @Setting val texts: Texts = Config.Texts()
) {
    @ConfigSerializable
    data class Unity(
            @Setting val member1: UUID = UUID(0, 0),
            @Setting val member2: UUID = UUID(0, 0),
            @Setting val home: Location<World>? = null
    ) {
        fun sendMessage(text: Text) {
            member1.getPlayer()?.sendMessage(text)
            member2.getPlayer()?.sendMessage(text)
        }

        fun getOtherMember(member: UUID) = when {
            member1 == member -> member2
            member2 == member -> member1
            else -> throw RuntimeException("Getting other member failed, report to developer!")
        }
    }

    @ConfigSerializable
    class Texts(
            @Setting val sentRequest: TextTemplate = fixedTextTemplateOf(
                    "Proposal sent to ".aqua(), "requestee".toArg().white(), "!".aqua()),

            @Setting val gotRequest: TextTemplate = fixedTextTemplateOf(
                    "requester".toArg().white(), " wants to marry you!".aqua()),
            @Setting val acceptRequestAction: Text = "[ACCEPT]".green(),
            @Setting val declineRequestAction: Text = "[DECLINE]".red(),

            @Setting val cancelledRequestMessage: TextTemplate = fixedTextTemplateOf(
                    "requester".toArg().white(), " cancelled the request!".aqua()
            ),

            @Setting val sentCancellation: TextTemplate = fixedTextTemplateOf(
                    "Sent cancellation to ".aqua(), "requestee".toArg().white(), "!".aqua()
            ),

            @Setting val requestBroadcast: TextTemplate = fixedTextTemplateOf(
                    "requester".toArg().white(), " sent a proposal to ".aqua(), "requestee".toArg().white(), "!".aqua()),

            @Setting val unityBroadcast: TextTemplate = fixedTextTemplateOf(
                    "member1".toArg().white(), " and ".aqua(), "member2".toArg().white(), " just got married!".aqua()),

            @Setting val declinedRequestBroadcast: TextTemplate = fixedTextTemplateOf(
                    "requestee".toArg().white(), " declined to marry ".aqua(), "requester".toArg().white(), "!".aqua()),

            @Setting val divorceBroadcast: TextTemplate = fixedTextTemplateOf(
                    "member1".toArg().white(), " just divorced from ".aqua(), "member2".toArg().white(), "!".aqua()),

            @Setting val listCommandTitle: Text = "Marriages".yellow(),
            @Setting val listCommandEntry: TextTemplate = fixedTextTemplateOf(
                    "- ".aqua(), "member1".toArg().white(), " is married to ".aqua(), "member2".toArg().white()),

            @Setting val sentGift: TextTemplate = fixedTextTemplateOf(
                    "Sent gift to ".aqua(), "otherMember".toArg().white(), "!".aqua()
            ),
            @Setting val receivedGift: TextTemplate = fixedTextTemplateOf(
                    "Received gift from".aqua(), "otherMember".toArg().white(), "!".aqua()
            ),

            @Setting val helpCommandTitle: Text = "Marriages".yellow(),
            @Setting val helpCommandEntries: List<Text> = listOf(
                    "- /marry help - Shows this page".toText(),
                    "- /marry <name> - Marry a player".toText(),
                    "- /marry accept - Accept the proposal".toText(),
                    "- /marry decline - Decline the proposal".toText(),
                    "- /marry divorce - Divorce from your partner".toText(),
                    "- /marry tp - Teleport to your partner".toText(),
                    "- /marry gift - Gift the item you are holding to your partner".toText(),
                    "- /marry home - Teleport to the couple's home".toText(),
                    "- /marry home set - Set the couple's home".toText()
            )
    )
}