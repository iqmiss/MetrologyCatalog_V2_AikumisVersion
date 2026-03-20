package com.catalog.repository;

import com.catalog.models.Result;
import com.catalog.utils.DatabaseUtil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ResultRepository {

    public List<Result> findAll() {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT * FROM results";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Result result = mapResultSetToResult(rs);
                results.add(result);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public Result findById(int id) {
        String sql = "SELECT * FROM results WHERE id = ?";
        Result result = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = mapResultSetToResult(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Result findByOrderId(int orderId) {
        String sql = "SELECT * FROM results WHERE order_id = ?";
        Result result = null;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = mapResultSetToResult(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Result> findByMetrologistId(int metrologistId) {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT * FROM results WHERE metrologist_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, metrologistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Result result = mapResultSetToResult(rs);
                    results.add(result);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public void save(Result result) {
        String sql = "INSERT INTO results (order_id, result_type, issued_at, file_path, metrologist_id, is_signed, signed_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, result.getOrderId());
            stmt.setString(2, result.getResultType());
            stmt.setObject(3, result.getIssuedAt());
            stmt.setString(4, result.getFilePath());
            stmt.setInt(5, result.getMetrologistId());
            stmt.setBoolean(6, result.isSigned());
            stmt.setObject(7, result.getSignedAt());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Result result) {
        String sql = "UPDATE results SET order_id = ?, result_type = ?, issued_at = ?, file_path = ?, metrologist_id = ?, is_signed = ?, signed_at = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, result.getOrderId());
            stmt.setString(2, result.getResultType());
            stmt.setObject(3, result.getIssuedAt());
            stmt.setString(4, result.getFilePath());
            stmt.setInt(5, result.getMetrologistId());
            stmt.setBoolean(6, result.isSigned());
            stmt.setObject(7, result.getSignedAt());
            stmt.setInt(8, result.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM results WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Result mapResultSetToResult(ResultSet rs) throws SQLException {
        Result result = new Result();
        result.setId(rs.getInt("id"));
        result.setOrderId(rs.getInt("order_id"));
        result.setResultType(rs.getString("result_type"));
        result.setIssuedAt(rs.getTimestamp("issued_at") != null ? rs.getTimestamp("issued_at").toLocalDateTime() : null);
        result.setFilePath(rs.getString("file_path"));
        result.setMetrologistId(rs.getInt("metrologist_id"));
        result.setSigned(rs.getBoolean("is_signed"));
        result.setSignedAt(rs.getTimestamp("signed_at") != null ? rs.getTimestamp("signed_at").toLocalDateTime() : null);
        return result;
    }
}