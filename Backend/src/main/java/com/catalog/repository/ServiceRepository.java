package com.catalog.repository;

import com.catalog.models.Service;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ServiceRepository {

    // JOIN с таблицей laboratories чтобы получить название лаборатории
    // и нормативный документ (ГОСТ) для отображения в каталоге
    public List<Service> findAll() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.*, l.name as lab_name FROM services s " +
                    "LEFT JOIN laboratories l ON s.lab_id = l.id " +
                    "WHERE s.is_active = true";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех услуг", e);
        }

        return services;
    }

    public Service findById(int id) {
        String sql = "SELECT s.*, l.name as lab_name FROM services s " +
                    "LEFT JOIN laboratories l ON s.lab_id = l.id " +
                    "WHERE s.id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToService(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении услуги id=" + id, e);
        }

        return null;
    }

    // Фильтрация услуг по типу средства измерений — используется в каталоге
    public List<Service> findByMeasurementType(String measurementType) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.*, l.name as lab_name FROM services s " +
                    "LEFT JOIN laboratories l ON s.lab_id = l.id " +
                    "WHERE s.measurement_type = ? AND s.is_active = true";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, measurementType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    services.add(mapResultSetToService(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при фильтрации услуг по типу=" + measurementType, e);
        }

        return services;
    }

    public List<Service> findByLabId(int labId) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.*, l.name as lab_name FROM services s " +
                    "LEFT JOIN laboratories l ON s.lab_id = l.id " +
                    "WHERE s.lab_id = ? AND s.is_active = true";

        try (Connection conn = DatabaseUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, labId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    services.add(mapResultSetToService(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении услуг лаборатории id=" + labId, e);
        }

        return services;
    }

    public void save(Service service) {
        String sql = "INSERT INTO services (name, description, measurement_type, price, duration_days, lab_id, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setString(3, service.getMeasurementType());
            stmt.setDouble(4, service.getPrice());
            stmt.setInt(5, service.getDurationDays());
            stmt.setInt(6, service.getLabId());
            stmt.setBoolean(7, service.isActive());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении услуги", e);
        }
    }

    public void update(Service service) {
        String sql = "UPDATE services SET name = ?, description = ?, measurement_type = ?, price = ?, duration_days = ?, lab_id = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setString(3, service.getMeasurementType());
            stmt.setDouble(4, service.getPrice());
            stmt.setInt(5, service.getDurationDays());
            stmt.setInt(6, service.getLabId());
            stmt.setBoolean(7, service.isActive());
            stmt.setInt(8, service.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении услуги id=" + service.getId(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM services WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении услуги id=" + id, e);
        }
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setId(rs.getInt("id"));
        service.setName(rs.getString("name"));
        service.setDescription(rs.getString("description"));
        service.setMeasurementType(rs.getString("measurement_type"));
        service.setPrice(rs.getDouble("price"));
        service.setDurationDays(rs.getInt("duration_days"));
        service.setLabId(rs.getInt("lab_id"));
        service.setActive(rs.getBoolean("is_active"));
        service.setStandard(rs.getString("standard"));
        service.setLabName(rs.getString("lab_name"));
        return service;
    }
}