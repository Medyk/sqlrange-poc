package com.example.sqlrange;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, BookId>, JpaSpecificationExecutor<Book> {
    @Modifying
    @Query("""
        UPDATE Book b
        SET b.revisionTo = CURRENT_TIMESTAMP
        WHERE b.id.uuid = :uuid
          AND b.id.revision = :revision
          AND b.revisionTo > CURRENT_TIMESTAMP
    """)
    int closeCurrentRevision(
            @Param("uuid") UUID uuid,
            @Param("revision") Integer revision
    );
}
