package ru.zveron.entity

import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import ru.zveron.domain.link.CommunicationLinkData
import javax.persistence.*

@Entity
data class CommunicationLink(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "communication_link_id_seq")
    @SequenceGenerator(name = "communication_link_id_seq", allocationSize = 1, initialValue = 1)
    val id: Long = 0,
    @Column(nullable = false)
    val communicationLinkId: String,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb", nullable = false)
    val data: CommunicationLinkData,
    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false, updatable = false)
    val profile: Profile,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as CommunicationLink

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "${this::class.simpleName}(id=$id, type=${data.type}, profileId=${profile.id})"
    }
}
