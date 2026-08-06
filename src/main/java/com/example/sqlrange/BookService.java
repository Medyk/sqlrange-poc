package com.example.sqlrange;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findBooksWithSpecs(Specification<Book> specification) {
        return bookRepository.findAll(specification);
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


    @Transactional
    public Book updateBook(UUID bookUuid, Integer currentRevision, String newTitle) {
        BookId currentId = new BookId(bookUuid, currentRevision);

        // 1. Fetch the existing book to get all current fields
        Book existingBook = bookRepository.findById(currentId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        // 2. Close the current active row via DB clock
        int updatedRows = bookRepository.closeCurrentRevision(bookUuid, currentRevision);

        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Book.class, currentId);
        }

        // 3. Prepare the new ID using toBuilder
        BookId nextId = currentId.toBuilder()
                .revision(currentRevision + 1)
                .build();

        // 4. Clone the existing book and overwrite the ID and Title
        Book nextRevision = existingBook.toBuilder()
                .id(nextId)
                .title(newTitle)
                .build();

        // 5. Insert and return the new row
        return bookRepository.save(nextRevision);
    }
}
