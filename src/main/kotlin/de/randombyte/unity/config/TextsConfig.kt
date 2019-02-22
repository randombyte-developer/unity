package de.randombyte.unity.config

import de.randombyte.kosp.extensions.*
import de.randombyte.unity.config.TextsConfig.Placeholders.MEMBER_1
import de.randombyte.unity.config.TextsConfig.Placeholders.MEMBER_2
import de.randombyte.unity.config.TextsConfig.Placeholders.OTHER_MEMBER
import de.randombyte.unity.config.TextsConfig.Placeholders.REQUESTEE
import de.randombyte.unity.config.TextsConfig.Placeholders.REQUESTER
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

@ConfigSerializable
class TextsConfig(
        @Setting("sent-request") val sentRequest: String =
                ("Proposal sent to ".aqua() + REQUESTEE.white() + "!".aqua()).serialize(),

        @Setting("got-request") val gotRequest: String =
                (REQUESTER.white() + " wants to marry you!".aqua()).serialize(),

        @Setting("accept-request-action") val acceptRequestAction: String = "[ACCEPT]".green().serialize(),

        @Setting("decline-request-action") val declineRequestAction: String = "[DECLINE]".red().serialize(),

        @Setting("cancelled-request-message") val cancelledRequestMessage: String =
                (REQUESTER.white() + " cancelled the request!".aqua()).serialize(),

        @Setting("sent-cancellation") val sentCancellation: String =
                ("Sent cancellation to ".aqua() + REQUESTEE.white() + "!".aqua()).serialize(),

        @Setting("request-broadcast") val requestBroadcast: String =
                (REQUESTER.white() + " sent a proposal to ".aqua() + REQUESTEE.white() + "!".aqua()).serialize(),

        @Setting("unity-broadcast") val unityBroadcast: String =
                (MEMBER_1.white() + " and ".aqua() + MEMBER_2.white() + " just got married!".aqua()).serialize(),

        @Setting("declined-request-broadcast") val declinedRequestBroadcast: String =
                (REQUESTEE.white() + " declined to marry ".aqua() + REQUESTER.white() + "!".aqua()).serialize(),

        @Setting("divorce-broadcast") val divorceBroadcast: String =
                (MEMBER_1.white() + " just divorced from ".aqua() + MEMBER_2.white() + "!".aqua()).serialize(),

        @Setting("list-command-title") val listCommandTitle: String = "Marriages".yellow().serialize(),

        @Setting("list-command-entry") val listCommandEntry: String =
                ("- ".aqua() + MEMBER_1.white() + " is married to ".aqua() + MEMBER_2.white()).serialize(),

        @Setting("sent-gift") val sentGift: String =
                ("Sent gift to ".aqua() + OTHER_MEMBER.white() + "!".aqua()).serialize(),

        @Setting("received-gift") val receivedGift: String =
                ("Received gift from ".aqua() + OTHER_MEMBER.white() + "!".aqua()).serialize(),

        @Setting("help-command-title") val helpCommandTitle: String = "Marriages".yellow().serialize(),
        @Setting("help-command-entries") val helpCommandEntries: List<String> = listOf(
                "- /marry help - Shows this page",
                "- /marry <name> - Marry a player",
                "- /marry accept - Accept the proposal" ,
                "- /marry decline - Decline the proposal" ,
                "- /marry list - List all couples" ,
                "- /marry divorce - Divorce from your partner" ,
                "- /marry tp - Teleport to your partner" ,
                "- /marry gift - Gift the item you are holding to your partner" ,
                "- /marry home - Teleport to the couple's home" ,
                "- /marry home set - Set the couple's home" 
        )
) {
    object Placeholders {
        val REQUESTER = "requester".asPlaceholder
        val REQUESTEE = "requestee".asPlaceholder
        val OTHER_MEMBER = "other_member".asPlaceholder
        val MEMBER_1 = "member_1".asPlaceholder
        val MEMBER_2 = "member_2".asPlaceholder

        private val String.asPlaceholder get() = "%$this%"
    }
}