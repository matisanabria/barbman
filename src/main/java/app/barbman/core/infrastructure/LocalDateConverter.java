package app.barbman.core.infrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Converts LocalDate to/from yyyy-MM-dd TEXT for SQLite compatibility.
 * SQLite stores dates as TEXT; Hibernate's SQLiteDialect defaults to epoch millis
 * which breaks CHECK constraints and date parsing.
 */
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public String convertToDatabaseColumn(LocalDate date) {
        return date != null ? date.format(FMT) : null;
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) return null;

        // Handle legacy epoch-millis values
        if (dbValue.matches("\\d{10,}")) {
            long millis = Long.parseLong(dbValue);
            return java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }

        return LocalDate.parse(dbValue, FMT);
    }
}
