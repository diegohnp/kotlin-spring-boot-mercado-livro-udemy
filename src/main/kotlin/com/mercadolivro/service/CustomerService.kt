package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Profile
import com.mercadolivro.excepiton.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val bookService: BookService,
    private val bCrypt: BCryptPasswordEncoder
) {

    fun getAll(name: String?, pageable: Pageable): Page<CustomerModel> {
        name?.let {
            return customerRepository.findByNameContaining(it, pageable)
        }

        return customerRepository.findAll(pageable)
    }

    fun findCustomerById(id: Int): CustomerModel {
        return customerRepository.findById(id).orElseThrow{ NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code) }
    }

    fun createCustomer(customer: CustomerModel) {
        val customerCopy = customer.copy(
            password = bCrypt.encode(customer.password),
            roles = setOf(Profile.CUSTOMER)
        )
        customerRepository.save(customerCopy)
    }

    fun update(customer: CustomerModel) {
        val id = customer.id!!
        if (!customerRepository.existsById(id)) {
            throw NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)
        }

        customerRepository.save(customer)
    }

    fun delete(id: Int) {
        val customer = findCustomerById(id)
        bookService.deleteByCustomer(customer)

        customer.status = CustomerStatus.INATIVO
        customerRepository.save(customer)
    }

    fun emailAvailable(email: String): Boolean {
        return !customerRepository.existsByEmail(email)
    }

    fun findByEmail(email: String): Optional<CustomerModel> {
        return customerRepository.findByEmail(email)
    }

    fun getGrantedAuthorities(customer: CustomerModel): MutableCollection<out GrantedAuthority>? {
        val authorities: MutableSet<SimpleGrantedAuthority> = HashSet()
        authorities.add(SimpleGrantedAuthority(customer.roles.toString()))
        return authorities
    }
}