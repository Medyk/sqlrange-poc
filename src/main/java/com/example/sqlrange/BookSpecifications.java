package com.example.sqlrange;

import io.hypersistence.utils.hibernate.type.range.Range;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class BookSpecifications {

    /**
     * Filtruje książki, których zakres 'revision_range_cc' pokrywa się (&&) z podanym zakresem dat.
     */
    public static Specification<Book> overlapsRange(final OffsetDateTime inSince, final OffsetDateTime inUntil) {
        return (root, query, cb) -> {
            final OffsetDateTime since = inSince == null ? OffsetDateTime.MIN : inSince;
            final OffsetDateTime until = inUntil == null ? OffsetDateTime.MAX : inUntil;

            // 1. Tworzymy obiekt Range przy użyciu Hypersistence Utils
            // Tworzymy zakres [since, until) - typowo dla zapytań
            // Możesz użyć Range.closed(), Range.open() itp. w zależności od potrzeb.
            Range<OffsetDateTime> queryRange;
            if (OffsetDateTime.MIN.equals(since) && OffsetDateTime.MAX.equals(until)) {
                queryRange = Range.infinite(OffsetDateTime.class);
            } else if (OffsetDateTime.MIN.equals(since)) {
                queryRange = Range.infiniteOpen(until);
            } else if (OffsetDateTime.MAX.equals(until)) {
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