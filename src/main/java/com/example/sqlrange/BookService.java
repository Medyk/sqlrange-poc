package com.example.sqlrange;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findBooksActiveInPeriod(OffsetDateTime start, OffsetDateTime end, Pageable pageable) {
        // Wywołanie
        Specification<Book> spec = BookSpecifications.overlapsRange(start, end);

        return bookRepository.findAll(spec, pageable).getContent();
    }
}
