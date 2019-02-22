package de.randombyte.unity

import java.util.*

class RequestsManager {
    // <requestee, requesters>
    val requests: MutableMap<UUID, MutableSet<UUID>> = mutableMapOf()

    fun addRequest(requestee: UUID, requester: UUID) = getRequests(requestee).add(requester)

    fun getRequests(requestee: UUID) = requests[requestee] ?: mutableSetOf()

    fun hasRequest(requestee: UUID, requester: UUID) = requester in getRequests(requestee)

    fun removeRequest(requestee: UUID, requester: UUID) = getRequests(requestee).remove(requester)
}