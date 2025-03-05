package com.example.backend.domain.groupcategory

import com.example.backend.domain.category.entity.Category
import com.example.backend.domain.group.entity.Group
import jakarta.persistence.*

@Entity
@Table(name = "group_category")
class GroupCategory(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
