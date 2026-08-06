package com.example.sqlrange;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/favicon.ico")
    public void favicon() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<Book> updateBook(
            @PathVariable UUID uuid,
            @RequestBody UpdateBookRequest request
    ) {
        try {
            // Call the service method
            Book updatedBook = bookService.updateBook(
                    uuid,
                    request.revision(),
                    request.title()
            );

            // Return the newly inserted row containing the incremented revision
            // and the database-generated timestamp fields
            return ResponseEntity.ok(updatedBook);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }


    /**
     * Get current revision of an object.
     *
     * @param uuid
     * @return
     */
    @GetMapping(value = "/{uuid}", produces = "application/json")
    public ResponseEntity<Book> getBook(
            @PathVariable UUID uuid
    ) {
        Specification<Book> spec1 = BookSpecifications.uuid(uuid);
        Specification<Book> spec2 = BookSpecifications.insideRange(OffsetDateTime.now());

        List<Book> result = bookService.findBooksWithSpecs(spec1.and(spec2));
        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result.getFirst());
    }

    /**
     * Get books.
     *
     * @return
     */
    @GetMapping(value = "/", produces = "application/json")
    public ResponseEntity<?> getBooks(
            @RequestParam(required = false) OffsetDateTime timestamp,
            @RequestParam(required = false) OffsetDateTime since, @RequestParam(required = false) OffsetDateTime until, @RequestParam(required = false, defaultValue = "true") boolean delta,
            @PageableDefault() Pageable pageable
    ) {
        if (timestamp != null) {
            return ResponseEntity.ok(bookService.findBooksAtTimestamp(timestamp, translateSortProperty(pageable)));
        }
        if (since != null && until != null && !since.isBefore(until)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body("BAD REQUEST");
        }
        return ResponseEntity.ok(bookService.findBooksActiveInPeriod(since, until, delta, translateSortProperty(pageable)));
    }

    private Pageable translateSortProperty(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> switch (order.getProperty()) {
                    case "uuid" -> new Sort.Order(order.getDirection(), "id.uuid");
                    case "revision" -> new Sort.Order(order.getDirection(), "id.revision");
                    case "published" -> new Sort.Order(order.getDirection(), "year");
                    default -> order;
                })
                .collect(Collectors.toList());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }
}
