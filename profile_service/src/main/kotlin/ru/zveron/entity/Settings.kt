package ru.zveron.entity

import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import ru.zveron.domain.ChannelsDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MapsId
import javax.persistence.OneToOne

@Entity
data class Settings(
    @Id
    @Column(nullable = false)
    var id: Long = 0,
    var searchAddressId: Long = -1,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    var channels: ChannelsDto,
    @OneToOne
    @MapsId
    val profile: Profile,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Settings

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "${this::class.simpleName}(id=$id)"
    }
}
