package com.example.sqlrange;

import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BookSpecifications {

    /*
    SELECT * FROM books WHERE uuid IS NOT NULL;
     */
    public static Specification<Book> notNull() {
        return (root, query, cb) -> cb.isNotNull(root.get("id").get("uuid"));
    }

    /*
    SELECT * FROM books WHERE uuid = :uuid
     */
    public static Specification<Book> uuid(UUID uuid) {
        return (root, query, cb) -> cb.equal(root.get("id").get("uuid"), uuid);
    }

    /*
    SELECT * FROM books WHERE revision = :revision
     */
    public static Specification<Book> revision(Integer revision) {
        return (root, query, cb) -> cb.equal(root.get("id").get("revision"), revision);
    }

    /*
    SELECT *
    FROM books
    WHERE
      (revision_from >= :since AND revision_from < :until
      OR
      revision_to >= :since AND revision_to < :until)
     */
    public static Specification<Book> deltaRange(final OffsetDateTime since, final OffsetDateTime until) {
        return (root, query, cb) -> {
            if (since == null && until == null) return null;

            Predicate fromInRange = cb.and( // Builder nie obsługuje Predicate = null
                    since == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("revisionFrom"), since),
                    until == null ? cb.conjunction() : cb.lessThan(root.get("revisionFrom"), until)
            );

            Predicate toInRange = cb.and( // Builder nie obsługuje Predicate = null
                    since == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("revisionTo"), since),
                    until == null ? cb.conjunction() : cb.lessThan(root.get("revisionTo"), until)
            );

            return cb.or(fromInRange, toInRange);
        };
    }

    /*
    SELECT * FROM books
    WHERE revision_range @> :timestamp
     */
    public static Specification<Book> insideRange(final OffsetDateTime timestamp) {
        return (root, query, cb) -> {
            if (timestamp == null) return null;

            return cb.isTrue(
                    cb.function(
                            "contains",
                            Boolean.class,
                            root.get("revisionRange"),
                            cb.literal(timestamp)
                    )
            );
        };

    }

    /**
     * Filtruje książki, których zakres 'revision_range_cc' pokrywa się (&&) z podanym zakresem dat.
     */
    public static Specification<Book> overlapsRange(final OffsetDateTime since, final OffsetDateTime until) {
        return (root, query, cb) -> {
            if (since == null && until == null) return null;

            // 1. Tworzymy obiekt Range przy użyciu Hypersistence Utils
            // Tworzymy zakres [since, until) - typowo dla zapytań
            // Możesz użyć Range.closed(), Range.open() itp. w zależności od potrzeb.
            Range<OffsetDateTime> queryRange;
            if (since == null) {
                queryRange = Range.infiniteOpen(until);
            } else if (until == null) {
                queryRange = Range.closedInfinite(since);
            } else {
                queryRange = Range.closedOpen(since, until);
            }

            // 2. Wywołujemy naszą funkcję "overlaps" (zdefiniowaną w Contributorze)
            // Hibernate automatycznie zmapuje obiekt 'queryRange' na 'tstzrange' w SQL
            return cb.isTrue(
                    cb.function(
                            "overlaps",       // Nazwa naszej funkcji wirtualnej
                            Boolean.class,    // Typ zwrotny
                            root.get("revisionRangeCc"), // Kolumna w bazie
                            cb.literal(queryRange.asString())       // Parametr (Range Java -> Range SQL)
                    )
            );
        };
    }
}