package ru.zveron.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import ru.zveron.constant.Gender
import ru.zveron.model.WaysOfCommunicating
import java.time.Instant
import javax.persistence.*

@Entity
@TypeDef(name = "json", typeClass = JsonType::class)
data class Lot(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lot_id_seq")
    @SequenceGenerator(name = "lot_id_seq", allocationSize = 1, initialValue = 1)
    var id: Long = 0,
    var title: String,
    var description: String,
    var price: Int,
    @Column(name = "lot_form_id")
    val lotFormId: Int,
    @Column(name = "date_creation")
    var dateCreation: Instant,
    var status: String,
    @Enumerated(EnumType.ORDINAL)
    var gender: Gender? = null,
    /**
     * Имеет значение null, если аккаунт удален
     */
    @Column(name = "seller_id")
    val sellerId: Long?,
    @Column(name = "category_id")
    val categoryId: Int,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    var waysOfCommunicating: WaysOfCommunicating,
    @Column(name = "address_id")
    val addressId: Long
) {
    @OneToMany(mappedBy = "lot", cascade = [CascadeType.ALL])
    var photos: MutableList<LotPhoto> = mutableListOf()

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "lot", cascade = [CascadeType.ALL])
    lateinit var statistics: LotStatistics

    @OneToMany(mappedBy = "lot", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    var possibleCustomers: MutableSet<PossibleCustomer> = mutableSetOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Lot

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return """
            Lot(id = $id , title = $title , description = $description , price = $price, dateCreation = $dateCreation)
                """
    }
}
