package com.mercadolivro.controller.request

import jakarta.validation.constraints.NotEmpty

class PostAuthenticationRequest(
    @field:NotEmpty(message = "Email cannot be null")
    var email: String,

    @field:NotEmpty(message = "Password cannot be null")
    var password: String
)
