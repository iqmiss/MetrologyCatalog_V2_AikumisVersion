package com.catalog.repository;

import com.catalog.models.Device;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DeviceRepository {

    public List<Device> findAll() {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT * FROM devices";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Device device = mapResultSetToDevice(rs);
                devices.add(device);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }

    public Device findById(int id) {
        String sql = "SELECT * FROM devices WHERE id = ?";
        Device device = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    device = mapResultSetToDevice(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return device;
    }

    public Device findBySerialNumber(String serialNumber) {
        String sql = "SELECT * FROM devices WHERE serial_number = ?";
        Device device = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, serialNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    device = mapResultSetToDevice(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return device;
    }

    public List<Device> findByCompanyId(int companyId) {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT * FROM devices WHERE company_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Device device = mapResultSetToDevice(rs);
                    devices.add(device);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }

    public List<Device> findByType(String type) {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT * FROM devices WHERE type = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Device device = mapResultSetToDevice(rs);
                    devices.add(device);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }

    public void save(Device device) {
        String sql = "INSERT INTO devices (company_id, type, model, serial_number, last_verified_at, next_verification_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, device.getCompanyId());
            stmt.setString(2, device.getType());
            stmt.setString(3, device.getModel());
            stmt.setString(4, device.getSerialNumber());
            stmt.setObject(5, device.getLastVerifiedAt());
            stmt.setObject(6, device.getNextVerificationDate());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Device device) {
        String sql = "UPDATE devices SET company_id = ?, type = ?, model = ?, serial_number = ?, last_verified_at = ?, next_verification_date = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, device.getCompanyId());
            stmt.setString(2, device.getType());
            stmt.setString(3, device.getModel());
            stmt.setString(4, device.getSerialNumber());
            stmt.setObject(5, device.getLastVerifiedAt());
            stmt.setObject(6, device.getNextVerificationDate());
            stmt.setInt(7, device.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM devices WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Device mapResultSetToDevice(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("id"));
        device.setCompanyId(rs.getInt("company_id"));
        device.setType(rs.getString("type"));
        device.setModel(rs.getString("model"));
        device.setSerialNumber(rs.getString("serial_number"));
        device.setLastVerifiedAt(rs.getTimestamp("last_verified_at") != null ? rs.getTimestamp("last_verified_at").toLocalDateTime() : null);
        device.setNextVerificationDate(rs.getDate("next_verification_date") != null ? rs.getDate("next_verification_date").toLocalDate() : null);
        return device;
    }
}