package app.barbman.core.repositories.history.service;

import app.barbman.core.dto.history.ServiceDetailDTO;
import app.barbman.core.repositories.DbBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ServiceHistoryRepositoryImpl implements ServiceHistoryRepository{

    private static final Logger logger = LogManager.getLogger(ServiceHistoryRepositoryImpl.class);
    private static final String PREFIX = "[SERVICE-HISTORY-REPO]";

    private static final String SQL = """
        SELECT
            sd.displayName       AS name,
            si.quantity          AS quantity,
            si.unit_price        AS unit_price,
            si.item_total        AS total
        FROM service_item si
        JOIN service_header sh ON si.service_header_id = sh.id
        JOIN service_definition sd ON si.service_definition_id = sd.id
        WHERE sh.sale_id = ?
        ORDER BY si.id
        """;

    @Override
    public List<ServiceDetailDTO> findServicesBySaleId(int saleId) {

        List<ServiceDetailDTO> list = new ArrayList<>();

        try (Connection db = DbBootstrap.connect();
             PreparedStatement ps = db.prepareStatement(SQL)) {

            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ServiceDetailDTO dto = new ServiceDetailDTO();
                dto.setName(rs.getString("name"));
                dto.setQuantity(rs.getInt("quantity"));
                dto.setUnitPrice(rs.getDouble("unit_price"));
                dto.setTotal(rs.getDouble("total"));
                list.add(dto);
            }

            logger.info("{} Loaded {} service items for saleId={}",
                    PREFIX, list.size(), saleId);

        } catch (Exception e) {
            logger.error("{} Error loading service history for saleId={}: {}",
                    PREFIX, saleId, e.getMessage());
        }

        return list;
    }

}
