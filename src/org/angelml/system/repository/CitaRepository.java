package org.angelml.system.repository;

import org.angelml.system.DB.DatabaseConnection;
import org.angelml.system.model.Cita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CitaRepository implements CitaInterface {

    @Override
    public List<Cita> findBetween(LocalDate from, LocalDate to, Integer tecnicoId) {
        String sql = """
                select c.id_cita, c.id_tecnico, c.titulo, c.descripcion, c.fecha, c.hora, c.completada,
                       t.nombre as tecnico_nombre
                from cita c
                join tecnico t on t.id_tecnico = c.id_tecnico
                where c.fecha between ? and ?
                """ + (tecnicoId == null ? "" : " and c.id_tecnico = ?") + " order by c.fecha, c.hora, c.id_cita";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from.toString());
            statement.setString(2, to.toString());
            if (tecnicoId != null) {
                statement.setInt(3, tecnicoId);
            }
            try (ResultSet result = statement.executeQuery()) {
                List<Cita> citas = new ArrayList<>();
                while (result.next()) {
                    citas.add(map(result));
                }
                return citas;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cargar la agenda.", e);
        }
    }

    @Override
    public int save(Cita cita) {
        String sql = "insert into cita (id_tecnico, titulo, descripcion, fecha, hora, completada) values (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, cita.getTecnicoId());
            statement.setString(2, cita.getTitulo());
            statement.setString(3, cita.getDescripcion());
            statement.setString(4, cita.getFecha().toString());
            statement.setString(5, cita.getHora().toString());
            statement.setInt(6, cita.isCompletada() ? 1 : 0);
            statement.executeUpdate();
            return DatabaseConnection.lastInsertId(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo guardar la cita.", e);
        }
    }

    @Override
    public void update(Cita cita) {
        String sql = "update cita set titulo = ?, descripcion = ?, fecha = ?, hora = ?, completada = ? where id_cita = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cita.getTitulo());
            statement.setString(2, cita.getDescripcion());
            statement.setString(3, cita.getFecha().toString());
            statement.setString(4, cita.getHora().toString());
            statement.setInt(5, cita.isCompletada() ? 1 : 0);
            statement.setInt(6, cita.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la cita.", e);
        }
    }

    @Override
    public void setCompleted(int id, boolean completed) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("update cita set completada = ? where id_cita = ?")) {
            statement.setInt(1, completed ? 1 : 0);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la cita.", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("delete from cita where id_cita = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la cita.", e);
        }
    }

    @Override
    public int countByDate(LocalDate date, Integer tecnicoId) {
        String sql = "select count(*) from cita where fecha = ? and completada = 0"
                + (tecnicoId == null ? "" : " and id_tecnico = ?");
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            if (tecnicoId != null) {
                statement.setInt(2, tecnicoId);
            }
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar las citas.", e);
        }
    }

    private Cita map(ResultSet result) throws SQLException {
        Cita cita = new Cita();
        cita.setId(result.getInt("id_cita"));
        cita.setTecnicoId(result.getInt("id_tecnico"));
        cita.setTitulo(result.getString("titulo"));
        cita.setDescripcion(result.getString("descripcion"));
        cita.setFecha(DatabaseConnection.toDate(result.getString("fecha")));
        cita.setHora(DatabaseConnection.toTime(result.getString("hora")));
        cita.setCompletada(result.getInt("completada") == 1);
        cita.setTecnicoNombre(result.getString("tecnico_nombre"));
        return cita;
    }
}
