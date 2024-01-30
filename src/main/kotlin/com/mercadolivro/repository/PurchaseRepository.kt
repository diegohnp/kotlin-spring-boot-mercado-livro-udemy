package com.mercadolivro.repository

import com.mercadolivro.model.PurchaseModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseRepository : JpaRepository<PurchaseModel, Int> {
}