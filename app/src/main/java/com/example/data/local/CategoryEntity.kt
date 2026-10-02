package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CategoryItem

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val iconName: String,
    val colorHex: String,
    val isGameCategory: Boolean,
    val count: Int
) {
    fun toCategoryItem(): CategoryItem = CategoryItem(
        id = id,
        title = title,
        iconName = iconName,
        colorHex = colorHex,
        isGameCategory = isGameCategory,
        count = count
    )

    companion object {
        fun fromCategoryItem(c: CategoryItem): CategoryEntity = CategoryEntity(
            id = c.id,
            title = c.title,
            iconName = c.iconName,
            colorHex = c.colorHex,
            isGameCategory = c.isGameCategory,
            count = c.count
        )
    }
}
