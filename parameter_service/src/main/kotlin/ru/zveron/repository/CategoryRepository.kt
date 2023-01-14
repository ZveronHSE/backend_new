package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.zveron.entity.Category

interface CategoryRepository : JpaRepository<Category, Int> {
    /**
     * Если мы выбираем категорию родительскую, то нужно также выбрать все его дочерние категории,
     * а не только по родительским, поэтому тут идет выборка по дереву вниз :D
     */
    @Query(
        value = """with recursive c (id, name, id_parent) as
            (
            select category.id, category.name, category.id_parent
            from category
            where category.id = :id
            
            union all
            
            select category.id, category.name, category.id_parent
            from c
                join category on c.id = category.id_parent
            )
                
            select id, name, id_parent
            from c 
        """,
        nativeQuery = true
    )
    fun getFamilyById(id: Int): List<Category>
}