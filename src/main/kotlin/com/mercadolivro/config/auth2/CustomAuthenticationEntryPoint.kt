package com.mercadolivro.config.auth2

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mercadolivro.controller.response.ErrorResponse
import com.mercadolivro.enums.Errors
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val errorResponse = ErrorResponse(HttpStatus.UNAUTHORIZED.value(), Errors.ML000.message, Errors.ML000.code, null)
        response.outputStream.print(jacksonObjectMapper().writeValueAsString(errorResponse))
    }
}