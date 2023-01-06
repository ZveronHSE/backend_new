package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Category

interface CategoryRepository : JpaRepository<Category, Int> {
    fun getAllByParent(parentCategory: Category): List<Category>
}