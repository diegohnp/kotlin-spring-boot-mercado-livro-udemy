package com.mercadolivro.controller


import com.mercadolivro.extension.toBookModel
import com.mercadolivro.extension.toBookResponse
import com.mercadolivro.service.BookService
import com.mercadolivro.service.CustomerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
    private val customerService: CustomerService
) {

    @GetMapping
    fun findAll(@PageableDefault(page = 0, size = 10) pageable: Pageable): Page<com.mercadolivro.controller.response.BookResponse> =
        bookService.findAll(pageable).map { it.toBookResponse() }

    @GetMapping("/actives")
    fun findActives(@PageableDefault(page = 0, size = 10) pageable: Pageable): Page<com.mercadolivro.controller.response.BookResponse> =
        bookService.findActives(pageable).map { it.toBookResponse() }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int) =
        bookService.findById(id).toBookResponse()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: com.mercadolivro.controller.request.PostBookRequest) {
        val customer = customerService.findCustomerById(request.customerId)
        bookService.create(request.toBookModel(customer))
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Int, @RequestBody request: com.mercadolivro.controller.request.PutBookRequest) {
        val bookSaved = bookService.findById(id)
        bookService.update(request.toBookModel(bookSaved))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) {
        bookService.delete(id)
    }
}