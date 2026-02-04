package app.barbman.core.util.legacy;

import app.barbman.core.dto.history.SaleHistoryDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LegacySaleRepository {
    private static final Logger logger = LogManager.getLogger(LegacySaleRepository.class);
    public List<SaleHistoryDTO> searchByDateRange(LocalDate start, LocalDate end) {
        List<SaleHistoryDTO> list = new ArrayList<>();

        // Usamos un INNER JOIN para traer el nombre del barbero desde su tabla
        String sql = """
        SELECT s.id, s.precio, s.fecha, s.forma_pago, s.observaciones, b.nombre as nombre_barbero
        FROM servicios_realizados s
        INNER JOIN barberos b ON s.barbero_id = b.id
        WHERE s.fecha BETWEEN ? AND ?
        """;

        logger.info("[LEGACY-REPO] Iniciando búsqueda con nombres de barberos entre {} y {}", start, end);

        try (Connection db = LegacyDatabase.getConnection()) {
            if (db == null) return list;

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setString(1, start.toString());
                ps.setString(2, end.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        SaleHistoryDTO dto = new SaleHistoryDTO();
                        dto.setSaleId(rs.getInt("id"));
                        dto.setTotal(rs.getDouble("precio"));
                        dto.setDate(LocalDate.parse(rs.getString("fecha")));
                        dto.setPaymentMethod(mapLegacyPayment(rs.getString("forma_pago")));

                        // Seteamos el nombre del barbero legacy en el campo UserName
                        // Le agrego el (Beta) para que sepas que viene de la otra DB
                        String nombreBarbero = rs.getString("nombre_barbero");
                        dto.setUserName(nombreBarbero + " (Beta)");

                        dto.setClientName(rs.getString("observaciones"));
                        dto.setBeta(true);
                        list.add(dto);
                    }
                    logger.info("[LEGACY-REPO] Se encontraron {} registros con nombres asignados.", count);
                }
            }
        } catch (Exception e) {
            logger.error("[LEGACY-REPO] Error crítico en la consulta con JOIN", e);
        }
        return list;
    }

    /**
     * Normaliza los métodos de pago legacy a los que espera tu Controller
     */
    private String mapLegacyPayment(String legacy) {
        if (legacy == null) return "cash";
        return switch (legacy.toLowerCase()) {
            case "efectivo" -> "cash";
            case "transferencia" -> "transfer";
            case "pos" -> "card"; // O "qr" según prefieras mapear el POS viejo
            default -> "cash";
        };
    }
}
