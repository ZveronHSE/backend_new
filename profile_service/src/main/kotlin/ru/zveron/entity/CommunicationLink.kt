package ru.zveron.entity

import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import ru.zveron.domain.link.CommunicationLinkData
import ru.zveron.domain.link.CommunicationLinkType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(
        name = "communication_link_id_type_constraint",
        columnNames = ["communicationLinkId", "type"]
    )]
)
data class CommunicationLink(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "communication_link_id_seq")
    @SequenceGenerator(name = "communication_link_id_seq", allocationSize = 1, initialValue = 10)
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
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    val type: CommunicationLinkType = data.type

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
