package ru.zveron.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import ru.zveron.domain.channel.ChannelsDto
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.MapsId
import javax.persistence.OneToOne

@Entity
@TypeDef(name = "json", typeClass = JsonType::class)
data class Settings(
    @Id
    @Column(nullable = false)
    val id: Long = 0,
    val searchAddressId: Long = -1,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    val channels: ChannelsDto,
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
