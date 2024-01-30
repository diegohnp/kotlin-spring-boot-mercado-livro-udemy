package com.mercadolivro.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.config.auth2.UserCustomDetails
import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.helper.buildCustomers
import com.mercadolivro.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
@WithMockUser
class CustomerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should return all customers`() {
        val customer1 = customerRepository.save(buildCustomers())
        val customer2 = customerRepository.save(buildCustomers())

        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(customer1.id))
            .andExpect(jsonPath("$.content[0].name").value(customer1.name))
            .andExpect(jsonPath("$.content[0].email").value(customer1.email))
            .andExpect(jsonPath("$.content[0].status").value(customer1.status.name))
            .andExpect(jsonPath("$.content[1].id").value(customer2.id))
            .andExpect(jsonPath("$.content[1].name").value(customer2.name))
            .andExpect(jsonPath("$.content[1].email").value(customer2.email))
            .andExpect(jsonPath("$.content[1].status").value(customer2.status.name))
    }

    @Test
    fun `should filter all customers by name when get all`() {
        val customer1 = customerRepository.save(buildCustomers(name = "Gustavo"))

        mockMvc.perform(get("/api/customers?name=Gus"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(customer1.id))
            .andExpect(jsonPath("$.content[0].name").value(customer1.name))
            .andExpect(jsonPath("$.content[0].email").value(customer1.email))
            .andExpect(jsonPath("$.content[0].status").value(customer1.status.name))
    }

    @Test
    fun `Should create customer`() {
        val request = PostCustomerRequest("fake name", "${Random().nextInt()}@fakeemail.com.br", password = "123456")
        mockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)

        val customers = customerRepository.findAll().toList()
        assertEquals(1, customers.size)
        assertEquals(request.name, customers[0].name)
        assertEquals(request.email, customers[0].email)
    }

    @Test
    fun `Should throw error when create customer has invalid information`() {
        val request = PostCustomerRequest("", "${Random().nextInt()}@fakeemail.com.br", password = "123456")
        mockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity)
        .andExpect(jsonPath("$.httpCode").value(422))
        .andExpect(jsonPath("$.message").value("Invalid Request"))
        .andExpect(jsonPath("$.internalCode").value("ML-001"))
    }

    @Test
    fun `Should get user by id when user has the same id`() {
        val customer = customerRepository.save(buildCustomers())

        mockMvc.perform(get("/api/customers/${customer.id}")
            .with(user(UserCustomDetails(customer))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(customer.id))
            .andExpect(jsonPath("$.name").value(customer.name))
            .andExpect(jsonPath("$.email").value(customer.email))
            .andExpect(jsonPath("$.status").value(customer.status.name))
    }

    @Test
    fun `should update customer`() {
        val customer = customerRepository.save(buildCustomers())
        val request = PutCustomerRequest("Gustavo", "emailpdate@email.com")

        mockMvc.perform(put("/api/customers/${customer.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent)

        val customers = customerRepository.findAll().toList()
        assertEquals(1, customers.size)
        assertEquals(request.name, customers[0].name)
        assertEquals(request.email, customers[0].email)
    }

    @Test
    fun `should not found when update customer not exists`() {
        val request = PutCustomerRequest("Gustavo", "emailpdate@email.com")

        mockMvc.perform(put("/api/customers/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.httpCode").value(404))
            .andExpect(jsonPath("$.message").value("Customer [1] not exists."))
            .andExpect(jsonPath("$.internalCode").value("ML-201"))
    }

    @Test
    fun `Should throw error when update customer has invalid information`() {
        val request = PutCustomerRequest("", "${Random().nextInt()}@fakeemail.com.br")
        mockMvc.perform(put("/api/customers/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.httpCode").value(422))
            .andExpect(jsonPath("$.message").value("Invalid Request"))
            .andExpect(jsonPath("$.internalCode").value("ML-001"))
    }

    @Test
    fun `Should delete customer`() {
        val customer = customerRepository.save(buildCustomers())

        mockMvc.perform(delete("/api/customers/${customer.id}"))
            .andExpect(status().isNoContent)

        val customerDeleted = customerRepository.findById(customer.id!!)
        assertEquals(CustomerStatus.INATIVO, customerDeleted.get().status)
    }

    @Test
    fun `Should return not found when delete customer customer exists`() {
        val customer = customerRepository.save(buildCustomers())

        mockMvc.perform(delete("/api/customers/1"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.httpCode").value(404))
            .andExpect(jsonPath("$.message").value("Customer [1] not exists."))
            .andExpect(jsonPath("$.internalCode").value("ML-201"))
    }
}