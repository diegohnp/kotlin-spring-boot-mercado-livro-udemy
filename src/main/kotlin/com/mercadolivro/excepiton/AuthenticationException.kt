package com.mercadolivro.excepiton

class AuthenticationException(override val message: String, val errorCode: String) : Exception()