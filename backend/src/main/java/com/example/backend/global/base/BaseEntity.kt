package com.example.backend.global.base

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * baseEntity
 * 엔티티별 created_at, modified_at을 관리하는 BaseEntity
 * @author 100minha
 */
//@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Column(name = "created_at")
    @CreatedDate
    var createdAt: LocalDateTime? = null
        protected set

    @Column(name = "modified_at")
    @LastModifiedDate
    var modifiedAt: LocalDateTime? = null
        protected set
}
