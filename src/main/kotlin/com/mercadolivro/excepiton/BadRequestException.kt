package com.mercadolivro.excepiton

class BadRequestException(
    override val message: String,
    val errorCode: String
): RuntimeException()