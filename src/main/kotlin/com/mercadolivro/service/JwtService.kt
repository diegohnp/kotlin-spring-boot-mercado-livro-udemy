package com.mercadolivro.service

import com.mercadolivro.model.CustomerModel
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import java.util.function.Function

@Service
class JwtService {

    @Value("\${app.security.jwt.secret-key}")
    private val secretKey: String? = null

    @Value("\${app.security.jwt.expiration}")
    private val jwtExpiration: Long = 0

    @Value("\${app.security.jwt.refresh-token.expiration}")
    private val refreshExpiration: Long = 0

    @Throws( SecurityException::class)
    fun extractUsername(token: String): String {
        return extractClaim<String>(token, Claims::getSubject)
    }

    fun extractClaims(token: String): Map<String, Any> {
        return extractAllClaims(token)
    }

    private fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims: Claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }


    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun generateToken(customer: CustomerModel): String {
        val map: MutableMap<String, Any> = HashMap()
        map["userId"] = customer.id.toString()
        map["role"] = customer.roles
        return generateToken(map, customer)
    }

    private fun generateToken(extraClaims: Map<String, Any>, customer: CustomerModel): String {
        return buildToken(extraClaims, customer, jwtExpiration)
    }

    fun generateRefreshToken(customer: CustomerModel): String {
        return buildToken(HashMap<String, Any>(), customer, refreshExpiration)
    }

    private fun buildToken(extraClaims: Map<String, Any>, customer: CustomerModel, expiration: Long): String {
        return try {
            Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(customer.email)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact()
        } catch (e: ExpiredJwtException) {
            throw JwtException("Token expired")
        } catch (e: SecurityException) {
            throw JwtException("Token Signature invalid")
        } catch (e: MalformedJwtException) {
            throw JwtException("Token Signature invalid")
        } catch (e: Exception) {
            throw JwtException("Token invalid")
        }
    }

    fun isTokenValid(token: String, customer: CustomerModel): Boolean {
        val username = extractUsername(token)
        return username == customer.email && !isTokenExpired(token)
    }

    fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim<Date>(token, Claims::getExpiration)
    }

    private fun getSignInKey(): Key {
        val keyBytes: ByteArray = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}