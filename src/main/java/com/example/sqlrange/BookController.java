package com.example.sqlrange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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
