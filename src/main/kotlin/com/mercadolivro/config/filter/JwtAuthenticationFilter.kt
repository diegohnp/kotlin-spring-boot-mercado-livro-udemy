package com.mercadolivro.config.filter

import com.mercadolivro.model.CustomerModel
import com.mercadolivro.service.CustomerService
import com.mercadolivro.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.json.JSONObject
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val customerService: CustomerService
): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.servletPath.contains("/api/oauth/token")) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        val userEmail: String
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt: String = authHeader.substring(7)
        try {
            userEmail = jwtService.extractUsername(jwt)
            if (SecurityContextHolder.getContext().authentication == null) {
                val customerOptional: Optional<CustomerModel> = customerService.findByEmail(userEmail)
                val customer: CustomerModel = customerOptional.orElseThrow {
                    NotFoundException()
                }
                if (jwtService.isTokenValid(jwt, customer)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        customer.email,
                        null,
                        customerService.getGrantedAuthorities(customer)
                    )
//                    val map: Map<String, Any> = jwtService.extractClaims(jwt)
//                    val tokenInformation: TokenInformation =
//                        tokenInformationService.convertMapToTokenInformationObject(map)
//                    authToken.details = tokenInformation
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            val jsonObject = JSONObject()
            jsonObject.put("error", jwt)
            jsonObject.put("error_description", ex.message)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write(jsonObject.toString())
        }
    }
}