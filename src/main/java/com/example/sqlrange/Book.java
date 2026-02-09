package com.example.sqlrange;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Book")
@Table(name = "books")
public class Book {
    @EmbeddedId
    @JsonUnwrapped
    private BookId id;
    @Column(name = "revision_from")
    private OffsetDateTime revisionFrom;
    @Column(name = "revision_to")
    private OffsetDateTime revisionTo;

    private String title;
    private String author;
    @Column(name = "pub_year")
    private Integer year;
    private String genre;

    // https://vladmihalcea.com/map-postgresql-range-column-type-jpa-hibernate/
    @Type(PostgreSQLRangeType.class)
    @Column(name = "revision_range", columnDefinition = "tstzrange", insertable = false, updatable = false)
    private Range<OffsetDateTime> revisionRange;

    @JsonGetter("revisionRange")
    public String getRevisionRangeAsString() {
        return revisionRange != null ? revisionRange.asString() : null;
    }

    @Type(PostgreSQLRangeType.class)
    @Column(name = "revision_range_cc", columnDefinition = "tstzrange", insertable = false, updatable = false)
    private Range<OffsetDateTime> revisionRangeCc;

    @JsonGetter("revisionRangeCc")
    public String getRevisionRangeCcAsString() {
        return revisionRangeCc != null ? revisionRangeCc.asString() : null;
    }
}
