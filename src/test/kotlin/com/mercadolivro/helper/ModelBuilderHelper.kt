package com.mercadolivro.helper

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Profile
import com.mercadolivro.model.BookModel
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.model.PurchaseModel
import java.math.BigDecimal
import java.util.*

fun buildCustomers(
    id: Int? = null,
    name: String = "Customer Name",
    email: String = "${UUID.randomUUID()}@email.com",
    password: String = "password"
) = CustomerModel(
    id = id,
    name = name,
    email = email,
    status = CustomerStatus.ATIVO,
    password = password,
    roles = setOf(Profile.CUSTOMER)
)

fun buildPurchase(
    id: Int? = null,
    customer:CustomerModel = buildCustomers(),
    books: MutableList<BookModel> = mutableListOf(),
    nfe: String? = UUID.randomUUID().toString(),
    price: BigDecimal = BigDecimal.TEN

) = PurchaseModel(
    id = id,
    customer = customer,
    books = books,
    nfe = nfe,
    price = price
)