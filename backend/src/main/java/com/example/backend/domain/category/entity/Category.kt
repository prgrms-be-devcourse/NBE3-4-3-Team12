package com.example.backend.domain.category.entity

import com.example.backend.domain.groupcategory.GroupCategory
import jakarta.persistence.*
import java.util.ArrayList

@Entity
@Table(name = "categories")
class Category(
    @Column var name: String,

    @Enumerated(EnumType.STRING)
    @Column var categoryType: CategoryType
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = true)
    val groupCategories: MutableList<GroupCategory> = ArrayList()

    fun modify(name: String, categoryType: CategoryType) {
        this.name = name
        this.categoryType = categoryType
    }
}
