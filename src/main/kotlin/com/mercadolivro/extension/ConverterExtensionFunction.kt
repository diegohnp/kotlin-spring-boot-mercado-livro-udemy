package com.mercadolivro.extension

import com.mercadolivro.model.BookModel
import com.mercadolivro.model.CustomerModel

fun com.mercadolivro.controller.request.PostCustomerRequest.toCustomerModel(): CustomerModel {
    return CustomerModel(
        name = this.name,
        email = this.email,
        status = com.mercadolivro.enums.CustomerStatus.ATIVO,
        password = this.password)
}

fun com.mercadolivro.controller.request.PutCustomerRequest.toCustomerModel(previousValue: CustomerModel): CustomerModel {
    return CustomerModel(
        id = previousValue.id,
        name = this.name,
        email = this.email,
        status = previousValue.status,
        password = previousValue.password)
}

fun com.mercadolivro.controller.request.PostBookRequest.toBookModel(customer: CustomerModel): BookModel {
    return BookModel(name = this.name,
        price = this.price,
        status = com.mercadolivro.enums.BookStatus.ATIVO,
        customer = customer

    )
}

fun com.mercadolivro.controller.request.PutBookRequest.toBookModel(previousValue: BookModel): BookModel {
    return BookModel(
        id = previousValue.id,
        name = this.name ?: previousValue.name,
        price = this.price ?: previousValue.price,
        status = previousValue.status,
        customer = previousValue.customer
    )
}

fun CustomerModel.toCustomerResponse(): com.mercadolivro.controller.response.CustomerResponse {
    return com.mercadolivro.controller.response.CustomerResponse(
        id = this.id,
        name = this.name,
        email = this.email,
        status = this.status
    )
}

fun BookModel.toBookResponse(): com.mercadolivro.controller.response.BookResponse {
    return com.mercadolivro.controller.response.BookResponse(
        id = this.id,
        name = this.name,
        price = this.price,
        customerId = this.customer!!.id,
        status = this.status
    )
}