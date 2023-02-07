package ru.zveron.domain.profile

const val COMMUNICATION_LINKS_INITIALIZATION_TYPE = "profile-communication-links-entity-graph"

enum class ProfileInitializationType(val graphName: String?) {
    DEFAULT(null),
    COMMUNICATION_LINKS(COMMUNICATION_LINKS_INITIALIZATION_TYPE),
    ;
}