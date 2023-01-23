package ru.zveron.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*

@Entity
@TypeDef(name = "json", typeClass = JsonType::class)
data class Parameter(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parameter_id_seq")
    @SequenceGenerator(name = "parameter_id_seq", allocationSize = 1, initialValue = 1)
    var id: Int = -1,
    @Column(length = 30)
    val name: String,
    val type: String,
    val isRequired: Boolean,
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    val list_value: List<String>?
)