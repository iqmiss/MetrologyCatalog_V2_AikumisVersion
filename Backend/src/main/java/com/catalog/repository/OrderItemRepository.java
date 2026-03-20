package com.catalog.repository;

import com.catalog.models.OrderItem;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderItemRepository {

    public List<OrderItem> findAll() {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToOrderItem(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех позиций заявок", e);
        }

        return items;
    }

    public OrderItem findById(int id) {
        String sql = "SELECT * FROM order_items WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToOrderItem(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении позиции заявки id=" + id, e);
        }

        return null;
    }

    // Получение всех приборов в заявке — один заказ может содержать несколько приборов
    public List<OrderItem> findByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToOrderItem(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении позиций заявки id=" + orderId, e);
        }

        return items;
    }

    public void save(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, device_type, model, serial_number, quantity, unit_price) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setString(2, item.getDeviceType());
            stmt.setString(3, item.getModel());
            stmt.setString(4, item.getSerialNumber());
            stmt.setInt(5, item.getQuantity());
            stmt.setDouble(6, item.getUnitPrice());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении позиции заявки", e);
        }
    }

    public void update(OrderItem item) {
        String sql = "UPDATE order_items SET order_id = ?, device_type = ?, model = ?, serial_number = ?, quantity = ?, unit_price = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setString(2, item.getDeviceType());
            stmt.setString(3, item.getModel());
            stmt.setString(4, item.getSerialNumber());
            stmt.setInt(5, item.getQuantity());
            stmt.setDouble(6, item.getUnitPrice());
            stmt.setInt(7, item.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении позиции заявки id=" + item.getId(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM order_items WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении позиции заявки id=" + id, e);
        }
    }

    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setDeviceType(rs.getString("device_type"));
        item.setModel(rs.getString("model"));
        item.setSerialNumber(rs.getString("serial_number"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        return item;
    }
}