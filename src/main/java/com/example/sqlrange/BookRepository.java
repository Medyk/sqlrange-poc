package com.example.sqlrange;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, BookId>, JpaSpecificationExecutor<Book> {
    // Nie musisz tu pisać żadnych metod!
}
