package com.mercadolivro.service

import com.mercadolivro.config.auth2.UsernamePwdAuthenticationProvider
import com.mercadolivro.controller.request.PostAuthenticationRequest
import com.mercadolivro.controller.response.AuthenticationResponse
import com.mercadolivro.model.CustomerModel
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticationService(
    private val customerService: CustomerService,
    private val usernamePasswordAuthenticationProvider: UsernamePwdAuthenticationProvider,
    private var jwtService: JwtService
) {

    fun authenticate(authentication: PostAuthenticationRequest): AuthenticationResponse {
        val customerOptional: Optional<CustomerModel> = customerService.findByEmail(authentication.email)
        if (customerOptional.isEmpty) {
            throw UsernameNotFoundException("User not found!")
        }
        usernamePasswordAuthenticationProvider.authenticate(
            UsernamePasswordAuthenticationToken(
                authentication.email,
                authentication.password
            )
        )
        val customer: CustomerModel = customerOptional.get()
        val jwtToken: String = jwtService.generateToken(customer)
        val refreshToken: String = jwtService.generateRefreshToken(customer)
        return AuthenticationResponse(jwtToken, refreshToken)
    }
}
