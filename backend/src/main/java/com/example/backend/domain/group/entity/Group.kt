package com.example.backend.domain.group.entity;

import com.example.backend.domain.groupcategory.GroupCategory;
import com.example.backend.domain.member.entity.Member;
import com.example.backend.global.base.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "\"groups\"")
class Group(
    @Column
    var title : String,

    @Column
    var description : String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member : Member,

    @Column
    @Enumerated(value = EnumType.STRING)
    var status : GroupStatus = GroupStatus.RECRUITING,

    @Column
    var maxParticipants : Int,

    ) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var groupCategories : MutableList<GroupCategory>

    fun update(title : String, description : String, maxParticipants : Int, status : GroupStatus) {
        this.title = title;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.status = status;
    }

    fun updateStatus(status : GroupStatus) {
        this.status = status;
    }

    fun addGroupCategories(groupCategories : MutableList<GroupCategory>) {
        this.groupCategories = groupCategories;
    }
}