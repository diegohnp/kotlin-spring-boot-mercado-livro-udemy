package com.mercadolivro.controller

import com.mercadolivro.controller.request.PostAuthenticationRequest
import com.mercadolivro.controller.response.AuthenticationResponse
import com.mercadolivro.service.AuthenticationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {

    @PostMapping("/token")
    fun authenticate(@RequestBody authentication: PostAuthenticationRequest): AuthenticationResponse {
        return authenticationService.authenticate(authentication)
    }
}