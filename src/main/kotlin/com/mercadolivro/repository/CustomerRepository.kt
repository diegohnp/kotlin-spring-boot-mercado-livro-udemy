package com.mercadolivro.repository

import com.mercadolivro.model.CustomerModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CustomerRepository : JpaRepository<CustomerModel, Int> {

    fun findByNameContaining(name: String, pageable: Pageable): Page<CustomerModel>

    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): Optional<CustomerModel>
}