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
    }

    @ConfigSerializable
    class Texts(
            @Setting val gotRequest: TextTemplate = fixedTextTemplateOf(
                    "requester".toArg().white(), " wants to marry you!".aqua()),
            @Setting val acceptRequestAction: Text = "[ACCEPT]".green(),
            @Setting val declineRequestAction: Text = "[DECLINE]".red(),

            @Setting val unityBroadcast: TextTemplate = fixedTextTemplateOf(
                    "member1".toArg().white(), " and ".aqua(), "member2".toArg().white(), " just got married!".aqua()),

            @Setting val divorceBroadcast: TextTemplate = fixedTextTemplateOf(
                    "member1".toArg().white(), " just divorced from ".aqua(), "member2".toArg().white(), "!".aqua()),

            @Setting val listCommandTitle: Text = "Marriages".yellow(),
            @Setting val listCommandEntry: TextTemplate = fixedTextTemplateOf(
                    "-".aqua(), "member1".toArg().white(), " is married to ".aqua(), "member2".toArg().white())
    )
}