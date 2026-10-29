package org.angelml.system.repository;

import org.angelml.system.DB.DatabaseConnection;
import org.angelml.system.model.Rol;
import org.angelml.system.model.Tecnico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TecnicoRepository implements TecnicoInterface {

    private static final String COLUMNS = "id_tecnico, nombre, email, password, rol, activo";

    @Override
    public List<Tecnico> findAll() {
        String sql = "select " + COLUMNS + " from tecnico order by nombre collate nocase";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Tecnico> tecnicos = new ArrayList<>();
            while (result.next()) {
                tecnicos.add(map(result));
            }
            return tecnicos;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cargar la lista de técnicos.", e);
        }
    }

    @Override
    public Optional<Tecnico> findById(int id) {
        return findOne("select " + COLUMNS + " from tecnico where id_tecnico = ?", String.valueOf(id));
    }

    @Override
    public Optional<Tecnico> findByEmail(String email) {
        return findOne("select " + COLUMNS + " from tecnico where email = ? collate nocase", email);
    }

    @Override
    public int save(Tecnico tecnico) {
        String sql = "insert into tecnico (nombre, email, password, rol, activo) values (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tecnico.getNombre());
            statement.setString(2, tecnico.getEmail());
            statement.setString(3, tecnico.getPassword());
            statement.setString(4, tecnico.getRol().name());
            statement.setInt(5, tecnico.isActivo() ? 1 : 0);
            statement.executeUpdate();
            return DatabaseConnection.lastInsertId(connection);
        } catch (SQLException e) {
            throw translate(e, "No se pudo registrar al técnico.");
        }
    }

    @Override
    public void update(Tecnico tecnico) {
        boolean changePassword = tecnico.getPassword() != null;
        String sql = changePassword
                ? "update tecnico set nombre = ?, email = ?, rol = ?, activo = ?, password = ? where id_tecnico = ?"
                : "update tecnico set nombre = ?, email = ?, rol = ?, activo = ? where id_tecnico = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tecnico.getNombre());
            statement.setString(2, tecnico.getEmail());
            statement.setString(3, tecnico.getRol().name());
            statement.setInt(4, tecnico.isActivo() ? 1 : 0);
            if (changePassword) {
                statement.setString(5, tecnico.getPassword());
                statement.setInt(6, tecnico.getId());
            } else {
                statement.setInt(5, tecnico.getId());
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw translate(e, "No se pudo actualizar al técnico.");
        }
    }

    @Override
    public void setActive(int id, boolean active) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("update tecnico set activo = ? where id_tecnico = ?")) {
            statement.setInt(1, active ? 1 : 0);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar el estado del técnico.", e);
        }
    }

    @Override
    public int count() {
        return scalar("select count(*) from tecnico");
    }

    @Override
    public int countActiveAdmins() {
        return scalar("select count(*) from tecnico where rol = 'ADMIN' and activo = 1");
    }

    private int scalar(String sql) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la base de datos.", e);
        }
    }

    private Optional<Tecnico> findOne(String sql, String parameter) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar al técnico.", e);
        }
    }

    private Tecnico map(ResultSet result) throws SQLException {
        Tecnico tecnico = new Tecnico();
        tecnico.setId(result.getInt("id_tecnico"));
        tecnico.setNombre(result.getString("nombre"));
        tecnico.setEmail(result.getString("email"));
        tecnico.setPassword(result.getString("password"));
        tecnico.setRol(Rol.valueOf(result.getString("rol")));
        tecnico.setActivo(result.getInt("activo") == 1);
        return tecnico;
    }

    private RuntimeException translate(SQLException e, String fallback) {
        if (e.getMessage() != null && e.getMessage().contains("tecnico.email")) {
            return new IllegalArgumentException("Ya existe un técnico con ese correo.");
        }
        return new IllegalStateException(fallback, e);
    }
}
