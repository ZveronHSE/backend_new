package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.zveron.entity.Category

@Repository
interface CategoryRepository : JpaRepository<Category, Int> {
    fun getAllByParent(parentCategory: Category): List<Category>
}