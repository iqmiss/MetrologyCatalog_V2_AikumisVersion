package com.catalog.repository;

import com.catalog.models.Contract;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContractRepository {

    public List<Contract> findAll() {
        List<Contract> contracts = new ArrayList<>();
        String sql = "SELECT * FROM contracts";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Contract contract = mapResultSetToContract(rs);
                contracts.add(contract);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contracts;
    }

    public Contract findById(int id) {
        String sql = "SELECT * FROM contracts WHERE id = ?";
        Contract contract = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    contract = mapResultSetToContract(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contract;
    }

    public Contract findByOrderId(int orderId) {
        String sql = "SELECT * FROM contracts WHERE order_id = ?";
        Contract contract = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    contract = mapResultSetToContract(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contract;
    }

    public void save(Contract contract) {
        String sql = "INSERT INTO contracts (order_id, contract_number, signed_at, file_path, is_signed, signed_by) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, contract.getOrderId());
            stmt.setString(2, contract.getContractNumber());
            stmt.setObject(3, contract.getSignedAt());
            stmt.setString(4, contract.getFilePath());
            stmt.setBoolean(5, contract.isSigned());
            stmt.setObject(6, contract.getSignedBy());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Contract contract) {
        String sql = "UPDATE contracts SET order_id = ?, contract_number = ?, signed_at = ?, file_path = ?, is_signed = ?, signed_by = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, contract.getOrderId());
            stmt.setString(2, contract.getContractNumber());
            stmt.setObject(3, contract.getSignedAt());
            stmt.setString(4, contract.getFilePath());
            stmt.setBoolean(5, contract.isSigned());
            stmt.setObject(6, contract.getSignedBy());
            stmt.setInt(7, contract.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM contracts WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Contract mapResultSetToContract(ResultSet rs) throws SQLException {
        Contract contract = new Contract();
        contract.setId(rs.getInt("id"));
        contract.setOrderId(rs.getInt("order_id"));
        contract.setContractNumber(rs.getString("contract_number"));
        contract.setSignedAt(rs.getTimestamp("signed_at") != null ? rs.getTimestamp("signed_at").toLocalDateTime() : null);
        contract.setFilePath(rs.getString("file_path"));
        contract.setSigned(rs.getBoolean("is_signed"));
        contract.setSignedBy((Integer) rs.getObject("signed_by"));
        return contract;
    }
}