package app.barbman.core.infrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts LocalDateTime to/from yyyy-MM-dd HH:mm:ss TEXT for SQLite compatibility.
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime dt) {
        return dt != null ? dt.format(FMT) : null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) return null;

        // Handle legacy epoch-millis values
        if (dbValue.matches("\\d{10,}")) {
            long millis = Long.parseLong(dbValue);
            return java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        // Handle ISO 8601 format stored by older versions (e.g. "2026-02-04T18:22:43.627487500")
        if (dbValue.contains("T")) {
            return LocalDateTime.parse(dbValue);
        }

        return LocalDateTime.parse(dbValue, FMT);
    }
}
