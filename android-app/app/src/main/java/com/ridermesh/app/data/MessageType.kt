package com.ridermesh.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MessageType {
    @SerialName("text")
    TEXT_MESSAGE,

    @SerialName("presence")
    PRESENCE_UPDATE,

    @SerialName("control")
    CONTROL,

    @SerialName("voice")
    VOICE_FRAME
}
