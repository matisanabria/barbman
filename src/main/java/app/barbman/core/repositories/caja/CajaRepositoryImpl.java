package app.barbman.core.repositories.caja;

import app.barbman.core.model.CajaDiaria;
import app.barbman.core.repositories.DbBootstrap;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CajaRepositoryImpl implements CajaRepository{
    private static final Logger logger = LogManager.getLogger(CajaRepositoryImpl.class);

    private static final String SELECT_BASE = """
        SELECT id, fecha, ingresos_total, egresos_total,
               efectivo, transferencia, pos, saldo_final
        FROM caja_diaria
    """;


    @Override
    public List<CajaDiaria> findAll() {
        List<CajaDiaria> list = new ArrayList<>();
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            logger.warn("Error al listar cierres de caja: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public CajaDiaria findById(int id) {
        String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al obtener caja id {}: {}", id, e.getMessage());
        }
        return null;
    }

    @Override
    public void save(CajaDiaria caja) {
        String sql = """
            INSERT INTO caja_diaria (fecha, ingresos_total, egresos_total,
                                     efectivo, transferencia, pos, saldo_final)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, caja.getFecha().toString());
            ps.setDouble(2, caja.getIngresosTotal());
            ps.setDouble(3, caja.getEgresosTotal());
            ps.setDouble(4, caja.getSaldoFinal());
            ps.setDouble(5, caja.getEfectivo());
            ps.setDouble(6, caja.getTransferencia());
            ps.setDouble(7, caja.getPos());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    caja.setId(keys.getInt(1));
                }
            }
        } catch (Exception e) {
            logger.warn("Error al guardar cierre de caja: {}", e.getMessage());
        }
    }

    @Override
    public void update(CajaDiaria caja) {
        String sql = """
            UPDATE caja_diaria
            SET fecha = ?, ingresos_total = ?, egresos_total = ?,
                efectivo = ?, transferencia = ?, pos = ?, saldo_final = ?
            WHERE id = ?
        """;

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, caja.getFecha().toString());
            ps.setDouble(2, caja.getIngresosTotal());
            ps.setDouble(3, caja.getEgresosTotal());
            ps.setDouble(4, caja.getSaldoFinal());
            ps.setDouble(5, caja.getEfectivo());
            ps.setDouble(6, caja.getTransferencia());
            ps.setDouble(7, caja.getPos());
            ps.setInt(8, caja.getId());

            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al actualizar caja id {}: {}", caja.getId(), e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM caja_diaria WHERE id = ?";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.warn("Error al borrar caja id {}: {}", id, e.getMessage());
        }
    }

    @Override
    public CajaDiaria findByFecha(LocalDate fecha) {
        String sql = SELECT_BASE + " WHERE fecha = ? LIMIT 1";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, fecha.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al buscar caja por fecha {}: {}", fecha, e.getMessage());
        }
        return null;
    }

    @Override
    public CajaDiaria findUltimaCajaAntes(LocalDate fecha) {
        String sql = SELECT_BASE + " WHERE fecha < ? ORDER BY fecha DESC LIMIT 1";
        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, fecha.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            logger.warn("Error al buscar última caja antes de {}: {}", fecha, e.getMessage());
        }
        return null;
    }

    private CajaDiaria mapRow(ResultSet rs) throws SQLException {
        return new CajaDiaria(
                rs.getInt("id"),
                LocalDate.parse(rs.getString("fecha")),
                rs.getDouble("ingresos_total"),
                rs.getDouble("egresos_total"),
                rs.getDouble("saldo_final"),
                rs.getDouble("efectivo"),
                rs.getDouble("transferencia"),
                rs.getDouble("pos")
        );
    }
}
