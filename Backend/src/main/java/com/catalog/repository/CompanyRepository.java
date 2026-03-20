package com.catalog.repository;

import com.catalog.models.Company;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CompanyRepository {

    public List<Company> findAll() {
        List<Company> companies = new ArrayList<>();
        String sql = "SELECT * FROM companies";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                companies.add(mapResultSetToCompany(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех компаний", e);
        }

        return companies;
    }

    public Company findById(int id) {
        String sql = "SELECT * FROM companies WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToCompany(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении компании id=" + id, e);
        }

        return null;
    }

    // Поиск компании по БИН — используется при регистрации
    // чтобы привязать нового пользователя к существующей компании
    public Company findByBin(String bin) {
        String sql = "SELECT * FROM companies WHERE bin = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToCompany(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске компании по БИН=" + bin, e);
        }

        return null;
    }

    public void save(Company company) {
        String sql = "INSERT INTO companies (bin, name, address, phone, email) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company.getBin());
            stmt.setString(2, company.getName());
            stmt.setString(3, company.getAddress());
            stmt.setString(4, company.getPhone());
            stmt.setString(5, company.getEmail());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении компании", e);
        }
    }

    public void update(Company company) {
        String sql = "UPDATE companies SET bin = ?, name = ?, address = ?, phone = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company.getBin());
            stmt.setString(2, company.getName());
            stmt.setString(3, company.getAddress());
            stmt.setString(4, company.getPhone());
            stmt.setString(5, company.getEmail());
            stmt.setInt(6, company.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении компании id=" + company.getId(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM companies WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении компании id=" + id, e);
        }
    }

    private Company mapResultSetToCompany(ResultSet rs) throws SQLException {
        Company company = new Company();
        company.setId(rs.getInt("id"));
        company.setBin(rs.getString("bin"));
        company.setName(rs.getString("name"));
        company.setAddress(rs.getString("address"));
        company.setPhone(rs.getString("phone"));
        company.setEmail(rs.getString("email"));
        return company;
    }
}