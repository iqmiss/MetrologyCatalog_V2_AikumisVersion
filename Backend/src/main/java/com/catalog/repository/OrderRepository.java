package com.catalog.repository;

import com.catalog.models.Order;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех заказов", e);
        }

        return orders;
    }

    public Order findById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении заказа id=" + id, e);
        }

        return null;
    }

    public List<Order> findByClientId(int clientId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE client_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении заказов клиента id=" + clientId, e);
        }

        return orders;
    }

    public List<Order> findByLabId(int labId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE lab_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, labId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении заказов лаборатории id=" + labId, e);
        }

        return orders;
    }

    public List<Order> findByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE `status` = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при фильтрации заказов по статусу=" + status, e);
        }

        return orders;
    }

    public Map<String, Object> getStats() {
        String sql = """
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed,
                SUM(CASE WHEN status = 'in_work' THEN 1 ELSE 0 END) as in_work,
                SUM(CASE WHEN status = 'new' THEN 1 ELSE 0 END) as new_orders,
                SUM(CASE WHEN status = 'awaiting_payment' THEN 1 ELSE 0 END) as awaiting_payment,
                SUM(CASE WHEN status = 'completed' THEN total_price ELSE 0 END) as total_revenue
            FROM orders
            """;

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("totalOrders", rs.getLong("total"));
                stats.put("completedOrders", rs.getLong("completed"));
                stats.put("inWorkOrders", rs.getLong("in_work"));
                stats.put("newOrders", rs.getLong("new_orders"));
                stats.put("awaitingPayment", rs.getLong("awaiting_payment"));
                stats.put("totalRevenue", rs.getDouble("total_revenue"));
                return stats;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении статистики заказов", e);
        }

        return new java.util.HashMap<>();
    }

    public void save(Order order) {
        String sql = "INSERT INTO orders (order_number, client_id, service_id, lab_id, `status`, total_price, due_date, metrologist_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, order.getOrderNumber());
            stmt.setInt(2, order.getClientId());
            stmt.setInt(3, order.getServiceId());
            stmt.setInt(4, order.getLabId());
            stmt.setString(5, order.getStatus());
            stmt.setDouble(6, order.getTotalPrice());
            stmt.setDate(7, java.sql.Date.valueOf(order.getDueDate()));
            stmt.setObject(8, order.getMetrologistId());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении заказа", e);
        }
    }

    public void update(Order order) {
        String sql = "UPDATE orders SET order_number = ?, client_id = ?, service_id = ?, lab_id = ?, `status` = ?, total_price = ?, due_date = ?, metrologist_id = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.getOrderNumber());
            stmt.setInt(2, order.getClientId());
            stmt.setInt(3, order.getServiceId());
            stmt.setInt(4, order.getLabId());
            stmt.setString(5, order.getStatus());
            stmt.setDouble(6, order.getTotalPrice());
            stmt.setDate(7, java.sql.Date.valueOf(order.getDueDate()));
            stmt.setObject(8, order.getMetrologistId());
            stmt.setInt(9, order.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении заказа id=" + order.getId(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении заказа id=" + id, e);
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setClientId(rs.getInt("client_id"));
        order.setServiceId(rs.getInt("service_id"));
        order.setLabId(rs.getInt("lab_id"));
        order.setStatus(rs.getString("status"));
        order.setTotalPrice(rs.getDouble("total_price"));
        order.setDueDate(rs.getDate("due_date").toLocalDate());
        order.setMetrologistId((Integer) rs.getObject("metrologist_id"));
        return order;
    }
}