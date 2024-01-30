package com.mercadolivro.repository

import com.mercadolivro.helper.buildCustomers
import com.mercadolivro.model.CustomerModel
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CustomerRepositoryTest {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @Test
    fun `Should return name containing`() {
        val marcos = customerRepository.save(buildCustomers(name = "Marcos"))
        val matheus = customerRepository.save(buildCustomers(name = "Matheus"))
        customerRepository.save(buildCustomers(name = "Alex"))
        val pageable = PageRequest.of(0, 10)
        mockkStatic(pageable::class)

        val customers = customerRepository.findByNameContaining("Ma", pageable = pageable).content
        assertEquals(listOf(marcos, matheus), customers)
    }

    @Nested
    inner class `exists by email` {
        @Test
        fun `should return true when email exists`() {
            val email = "email@teste.com"
            customerRepository.save(buildCustomers(email = email))

            val exists = customerRepository.existsByEmail(email)

            assertTrue(exists)
        }

        @Test
        fun `should return false when email do not exists`() {
            val email = "naoexisteemail@teste.com"

            val exists = customerRepository.existsByEmail(email)

            assertFalse(exists)
        }
    }

    @Nested
    inner class `find by email` {
        @Test
        fun `should return customer by email`() {
            val email = "email@teste.com"
            val fakeCustomer = buildCustomers(email = email)
            customerRepository.save(fakeCustomer)

            val customer: Optional<CustomerModel> = customerRepository.findByEmail(email)

            assertNotNull(customer.get())
            assertEquals(fakeCustomer, customer.get())
        }

        @Test
        fun `should return null when email do not exists`() {
            val email = "naoexisteemail@teste.com"

            val result = customerRepository.findByEmail(email)

            assertTrue(result.isEmpty)
        }
    }
}