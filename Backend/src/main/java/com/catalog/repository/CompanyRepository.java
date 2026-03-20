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
                Company company = new Company();
                company.setId(rs.getInt("id"));
                company.setBin(rs.getString("bin"));
                company.setName(rs.getString("name"));
                company.setAddress(rs.getString("address"));
                company.setPhone(rs.getString("phone"));
                company.setEmail(rs.getString("email"));
                companies.add(company);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return companies;
    }

    public Company findById(int id) {
        String sql = "SELECT * FROM companies WHERE id = ?";
        Company company = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    company = new Company();
                    company.setId(rs.getInt("id"));
                    company.setBin(rs.getString("bin"));
                    company.setName(rs.getString("name"));
                    company.setAddress(rs.getString("address"));
                    company.setPhone(rs.getString("phone"));
                    company.setEmail(rs.getString("email"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return company;
    }

    public Company findByBin(String bin) {
        String sql = "SELECT * FROM companies WHERE bin = ?";
        Company company = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    company = new Company();
                    company.setId(rs.getInt("id"));
                    company.setBin(rs.getString("bin"));
                    company.setName(rs.getString("name"));
                    company.setAddress(rs.getString("address"));
                    company.setPhone(rs.getString("phone"));
                    company.setEmail(rs.getString("email"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return company;
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM companies WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}