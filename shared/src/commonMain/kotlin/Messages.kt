import kotlinx.serialization.Serializable

sealed class Messages {

    @Serializable
    data class Request(val req: String)

    @Serializable
    data class Response(val res: String)
}

