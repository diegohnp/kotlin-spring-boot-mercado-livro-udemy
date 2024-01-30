package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.excepiton.NotFoundException
import com.mercadolivro.helper.buildCustomers
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @MockK
    private lateinit var pageable: Pageable

    @InjectMockKs
    @SpyK
    private lateinit var customerService: CustomerService

    @Test
    fun `should return all customers`() {
        val fakeCustomers = PageImpl(listOf(buildCustomers(), buildCustomers()))

        every { customerRepository.findAll(pageable) } returns fakeCustomers

        val customers = customerService.getAll(null, pageable)

        assertEquals(fakeCustomers, customers)
        verify(exactly = 1) { customerRepository.findAll(pageable) }
        verify(exactly = 0) { customerRepository.findByNameContaining(any(), pageable) }
    }

    @Test
    fun `should return customers when name is informed`() {
        val name = UUID.randomUUID().toString()
        val fakeCustomers = PageImpl(listOf(buildCustomers(), buildCustomers()))

        every { customerRepository.findByNameContaining(name, pageable) } returns fakeCustomers

        val customers = customerService.getAll(name, pageable)

        assertEquals(fakeCustomers, customers)
        verify(exactly = 0) { customerRepository.findAll(pageable) }
        verify(exactly = 1) { customerRepository.findByNameContaining(name, pageable) }
    }

    @Test
    fun `Should create customer and encrypt password`() {
        val initialPassword = Random().nextInt().toString()
        val fakeCustomer = buildCustomers(password = initialPassword)
        val fakePassword = UUID.randomUUID().toString()
        val fakeCustomerEncrypted = fakeCustomer.copy(password = fakePassword)

        every { customerRepository.save(fakeCustomerEncrypted) } returns fakeCustomer
        every { bCrypt.encode(initialPassword)} returns fakePassword

        customerService.createCustomer(fakeCustomer)

        verify(exactly = 1) { customerRepository.save(fakeCustomerEncrypted) }
        verify(exactly = 1) { bCrypt.encode(initialPassword) }
    }

    @Test
    fun `Should return customer by id`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerRepository.findById(id) } returns Optional.of(fakeCustomer)

        val customer = customerService.findCustomerById(id)

        assertEquals(fakeCustomer, customer)
        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `Should throw customer not found when find by id`() {
        val id = Random().nextInt()

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> {
            customerService.findCustomerById(id)
        }

        assertEquals("Customer [${id}] not exists.", error.message)
        assertEquals("ML-201", error.errorCode)
        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `Should update customer`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerRepository.existsById(id) } returns true
        every { customerRepository.save(fakeCustomer) } returns fakeCustomer

        customerService.update(fakeCustomer)
        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 1) { customerRepository.save(fakeCustomer) }
    }

    @Test
    fun `Should throw customer not found exception when update customer`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerRepository.existsById(id) } returns false
        every { customerRepository.save(fakeCustomer) } returns fakeCustomer

        val error = assertThrows<NotFoundException> {
            customerService.update(fakeCustomer)
        }

        assertEquals("Customer [${id}] not exists.", error.message)
        assertEquals("ML-201", error.errorCode)
        verify(exactly = 1) { customerRepository.existsById(id) }
        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `Should delete customer by id`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)
        val expectedCustomer = fakeCustomer.copy(status = CustomerStatus.INATIVO)

        every { customerService.findCustomerById(id) } returns fakeCustomer
        every { customerRepository.save(expectedCustomer) } returns expectedCustomer
        every { bookService.deleteByCustomer(fakeCustomer) } just runs

        customerService.delete(id)

        verify(exactly = 1) { customerService.findCustomerById(id) }
        verify(exactly = 1) { bookService.deleteByCustomer(fakeCustomer) }
        verify(exactly = 1) { customerRepository.save(expectedCustomer) }

    }

    @Test
    fun `Should throw customer not found exception when delete customer by id`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomers(id = id)

        every { customerService.findCustomerById(id) } throws NotFoundException(Errors.ML201.message.format(id), Errors.ML201.code)

        val error = assertThrows<NotFoundException> {
            customerService.delete(id)
        }

        assertEquals("Customer [${id}] not exists.", error.message)
        assertEquals("ML-201", error.errorCode)

        verify(exactly = 1) { customerService.findCustomerById(id) }
        verify(exactly = 0) { bookService.deleteByCustomer(fakeCustomer) }
        verify(exactly = 0) { customerRepository.save(any()) }
    }

    @Test
    fun `Should return true when email available`() {
        val email = "${Random().nextInt()}@email.com"

        every { customerRepository.existsByEmail(email) } returns false

        val emailAvailable = customerService.emailAvailable(email)

        assertTrue(emailAvailable)
        verify(exactly = 1) { customerRepository.existsByEmail(email) }
    }

    @Test
    fun `Should return false when email unavailable`() {
        val email = "${Random().nextInt()}@email.com"

        every { customerRepository.existsByEmail(email) } returns true

        val emailAvailable = customerService.emailAvailable(email)

        assertFalse(emailAvailable)
        verify(exactly = 1) { customerRepository.existsByEmail(email) }
    }

    @Test
    fun `Should return customer by email`() {
        val email = "${Random().nextInt()}@email.com"
        val fakeCustomer = buildCustomers(email = email)

        every { customerRepository.findByEmail(email) } returns Optional.of(fakeCustomer)

        val customer:Optional<CustomerModel> = customerService.findByEmail(email)

        assertEquals(fakeCustomer, customer.get())
        verify(exactly = 1) { customerRepository.findByEmail(email) }
    }

    @Test
    fun `Should return customer null by email`() {
        val email = "${Random().nextInt()}@email.com"

        every { customerRepository.findByEmail(email) } returns Optional.empty()

        val customer:Optional<CustomerModel> = customerService.findByEmail(email)

        assertTrue(customer.isEmpty)
        verify(exactly = 1) { customerRepository.findByEmail(email) }
    }
}