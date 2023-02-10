package ru.zveron.entity

import org.hibernate.Hibernate
import ru.zveron.domain.profile.COMMUNICATION_LINKS_INITIALIZATION_TYPE
import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedEntityGraphs
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.SequenceGenerator

@Entity
@NamedEntityGraphs(
    NamedEntityGraph(
        name = COMMUNICATION_LINKS_INITIALIZATION_TYPE,
        attributeNodes = [NamedAttributeNode(value = "communicationLinks")]
    )
)
data class Profile(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_id_seq")
    @SequenceGenerator(name = "profile_id_seq", allocationSize = 1, initialValue = 10)
    val id: Long = 0,
    @Column(nullable = false, length = 50)
    val name: String,
    @Column(nullable = false, length = 50)
    val surname: String,
    val imageId: Long,
    @Column(nullable = false)
    val lastSeen: Instant,
    val addressId: Long = -1,
) {

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "profile", cascade = [CascadeType.ALL])
    lateinit var settings: Settings

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "profile")
    val communicationLinks: MutableList<CommunicationLink> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        return id == (other as Profile).id
    }

    override fun hashCode(): Int = id.hashCode()

    @Override
    override fun toString(): String {
        return "${this::class.simpleName}(id = $id)"
    }
}