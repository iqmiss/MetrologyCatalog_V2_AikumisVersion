package com.catalog.repository;

import com.catalog.models.Laboratory;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LaboratoryRepository {

    public List<Laboratory> findAll() {
        List<Laboratory> laboratories = new ArrayList<>();
        String sql = "SELECT * FROM laboratories";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Laboratory lab = new Laboratory();
                lab.setId(rs.getInt("id"));
                lab.setName(rs.getString("name"));
                lab.setAddress(rs.getString("address"));
                lab.setPhone(rs.getString("phone"));
                lab.setCity(rs.getString("city"));
                lab.setEmail(rs.getString("email"));
                laboratories.add(lab);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return laboratories;
    }

    public Laboratory findById(int id) {
        String sql = "SELECT * FROM laboratories WHERE id = ?";
        Laboratory lab = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    lab = new Laboratory();
                    lab.setId(rs.getInt("id"));
                    lab.setName(rs.getString("name"));
                    lab.setAddress(rs.getString("address"));
                    lab.setPhone(rs.getString("phone"));
                    lab.setCity(rs.getString("city"));
                    lab.setEmail(rs.getString("email"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lab;
    }

    public List<Laboratory> findByCity(String city) {
        List<Laboratory> laboratories = new ArrayList<>();
        String sql = "SELECT * FROM laboratories WHERE city = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, city);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Laboratory lab = new Laboratory();
                    lab.setId(rs.getInt("id"));
                    lab.setName(rs.getString("name"));
                    lab.setAddress(rs.getString("address"));
                    lab.setPhone(rs.getString("phone"));
                    lab.setCity(rs.getString("city"));
                    lab.setEmail(rs.getString("email"));
                    laboratories.add(lab);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return laboratories;
    }

    public void save(Laboratory lab) {
        String sql = "INSERT INTO laboratories (name, address, phone, city, email) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lab.getName());
            stmt.setString(2, lab.getAddress());
            stmt.setString(3, lab.getPhone());
            stmt.setString(4, lab.getCity());
            stmt.setString(5, lab.getEmail());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Laboratory lab) {
        String sql = "UPDATE laboratories SET name = ?, address = ?, phone = ?, city = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lab.getName());
            stmt.setString(2, lab.getAddress());
            stmt.setString(3, lab.getPhone());
            stmt.setString(4, lab.getCity());
            stmt.setString(5, lab.getEmail());
            stmt.setInt(6, lab.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM laboratories WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}