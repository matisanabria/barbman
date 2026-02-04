package app.barbman.core.util.legacy;

import app.barbman.core.model.Expense;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LegacyExpenseRepository {
    private static final Logger logger = LogManager.getLogger(LegacyExpenseRepository.class);

    public List<Expense> findAll() {
        List<Expense> list = new ArrayList<>();
        // Basado en tu estructura anterior: id, descripcion, monto, fecha, tipo, forma_pago
        String sql = "SELECT id, descripcion, monto, fecha, tipo, forma_pago FROM egresos";

        try (Connection db = LegacyDatabase.getConnection()) {
            if (db == null) return list;

            try (PreparedStatement ps = db.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    // Mapeamos al modelo nuevo 'Expense'
                    Expense expense = new Expense(
                            rs.getString("descripcion"),
                            rs.getDouble("monto"),
                            LocalDate.parse(rs.getString("fecha")),
                            mapLegacyType(rs.getString("tipo")),
                            mapLegacyPayment(rs.getString("forma_pago"))
                    );
                    expense.setId(rs.getInt("id"));
                    // Marcamos como beta si tu modelo Expense tiene ese flag,
                    // si no, lo dejamos así para que solo sea lectura.
                    list.add(expense);
                }
            }
        } catch (Exception e) {
            logger.error("[LEGACY-EXP-REPO] Error al listar egresos antiguos: {}", e.getMessage());
        }
        return list;
    }

    private String mapLegacyType(String legacyType) {
        if (legacyType == null) return "other";
        // Normalizamos los tipos para que tu ExpensesViewController.translateExpenseType los reconozca
        return switch (legacyType.toLowerCase()) {
            case "insumos" -> "supply";
            case "servicio", "servicios" -> "service";
            case "compra", "compras" -> "purchase";
            case "impuesto", "fijo" -> "tax";
            case "adelanto" -> "advance";
            case "salario" -> "salary";
            default -> "other";
        };
    }

    private int mapLegacyPayment(String legacyPayment) {
        if (legacyPayment == null) return 0; // cash por defecto
        return switch (legacyPayment.toLowerCase()) {
            case "efectivo" -> 0;      // cash
            case "transferencia" -> 1; // transfer
            case "pos", "tarjeta" -> 2; // card
            default -> 0;
        };
    }
}