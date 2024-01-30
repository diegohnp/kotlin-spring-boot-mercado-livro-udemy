package com.mercadolivro.config.auth2

import com.mercadolivro.model.CustomerModel
import com.mercadolivro.service.CustomerService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

@Component
class UsernamePwdAuthenticationProvider(
    private val customerService: CustomerService,
    private val bCrypt: BCryptPasswordEncoder
): AuthenticationProvider {
    override fun authenticate(authentication: Authentication?): Authentication {
        val email: String = authentication!!.name
        val pwd: String = authentication.credentials.toString()
        val customerOptional: Optional<CustomerModel> = customerService.findByEmail(email)
        if (customerOptional.isEmpty) {
            throw BadCredentialsException("No user registered with this details!")
        }

        val customer: CustomerModel = customerOptional.get()
        if (!bCrypt.matches(pwd, customer.password)) {
            throw BadCredentialsException("invalid password!")
        }

        return UsernamePasswordAuthenticationToken(email, pwd, customerService.getGrantedAuthorities(customer))
    }

    override fun supports(authentication: Class<*>?): Boolean {
        TODO("Not yet implemented")
    }
}