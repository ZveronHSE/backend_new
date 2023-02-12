package ru.zveron.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import ru.zveron.model.ChannelType
import ru.zveron.model.enum.Gender
import ru.zveron.model.enum.LotStatus
import java.time.Instant
import javax.persistence.*

@Entity
@TypeDef(name = "json", typeClass = JsonType::class)
data class Lot(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lot_id_seq")
    @SequenceGenerator(name = "lot_id_seq", allocationSize = 1, initialValue = 1)
    val id: Long = -1,
    var title: String,
    var description: String,
    var price: Int,
    @Column(name = "lot_form_id")
    val lotFormId: Int,
    @Column(name = "created_at")
    val createdAt: Instant,
    @Enumerated(EnumType.STRING)
    var status: LotStatus,
    @Enumerated(EnumType.STRING)
    val gender: Gender? = null,
    /**
     * Имеет значение null, если аккаунт удален
     */
    @Column(name = "seller_id")
    val sellerId: Long?,
    @Column(name = "category_id")
    val categoryId: Int,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    var channelType: ChannelType,
    @Column(name = "address_id")
    val addressId: Long
) {
    @OneToMany(mappedBy = "lot", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    lateinit var photos: List<LotPhoto>

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "lot", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    lateinit var statistics: LotStatistics

    @OneToMany(mappedBy = "lot", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    lateinit var parameters: List<LotParameter>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Lot

        return id == other.id && title == other.title && price == other.price && categoryId == other.categoryId
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return """
            Lot(id = $id , title = $title , description = $description , price = $price, createdAt = $createdAt)
                """
    }
}
