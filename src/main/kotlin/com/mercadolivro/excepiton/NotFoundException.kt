package com.mercadolivro.excepiton

class NotFoundException(
    override val message: String,
    val errorCode: String
): RuntimeException()