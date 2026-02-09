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

    public List<Book> findBooksAtTimestamp(OffsetDateTime timestamp, Pageable pageable) {
        Specification<Book> spec = BookSpecifications.insideRange(timestamp);

        return bookRepository.findAll(spec, pageable).getContent();
    }


    public List<Book> findBooksActiveInPeriod(OffsetDateTime start, OffsetDateTime end, boolean delta, Pageable pageable) {
        // Wywołanie
        Specification<Book> spec1 = BookSpecifications.notNull();
        Specification<Book> spec2 = delta ? BookSpecifications.deltaRange(start, end) : BookSpecifications.overlapsRange(start, end);
        Specification<Book> spec = spec1.and(spec2); // Spec obsługuje Predicate = null

        return bookRepository.findAll(spec, pageable).getContent();
    }
}
