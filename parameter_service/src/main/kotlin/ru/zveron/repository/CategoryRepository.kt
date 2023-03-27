package ru.zveron.repository

import io.grpc.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.zveron.entity.Category
import ru.zveron.exception.CategoryException

@JvmDefaultWithCompatibility
interface CategoryRepository : JpaRepository<Category, Int> {
    /**
     * Если мы выбираем категорию родительскую, то нужно также выбрать все его дочерние категории,
     * а не только по родительским, поэтому тут идет выборка по дереву вниз :D
     */
    @Query(
        value = """with recursive c (id, name, id_parent, image_url) as
            (
            select category.id, category.name, category.id_parent, category.image_url
            from category
            where category.id = :id
            
            union all
            
            select category.id, category.name, category.id_parent, category.image_url
            from c
                join category on c.id = category.id_parent
            )
                
            select id, name, id_parent, image_url
            from c 
        """,
        nativeQuery = true
    )
    fun getTreeById(id: Int): List<Category>


    fun getCategoriesByParentIsNull(): List<Category>

    fun getCategoryByIDOrThrow(categoryId: Int): Category = findById(categoryId)
        .orElseThrow { CategoryException(Status.NOT_FOUND, "Категории с id=$categoryId не существует") }

    fun getCategoriesByParent_IdEquals(categoryId: Int): List<Category>
}