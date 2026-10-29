package org.angelml.system.repository;

import org.angelml.system.DB.DatabaseConnection;
import org.angelml.system.config.AppConfig;
import org.angelml.system.model.EstadoServicio;
import org.angelml.system.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketRepository implements TicketInterface {

    private static final String SELECT = """
            select k.id_ticket, k.id_dispositivo, k.id_tecnico, k.folio, k.descripcion, k.estado, k.fecha_ingreso,
                   k.fecha_completado, k.impreso, k.fecha_impresion, d.tipo, d.marca, d.modelo, d.cliente,
                   t.nombre as tecnico_nombre
            from ticket k
            join dispositivo d on d.id_dispositivo = k.id_dispositivo
            left join tecnico t on t.id_tecnico = k.id_tecnico
            """;

    private static final String SYNC_DEVICE_STATUS = """
            update dispositivo set estado = case
                when exists (select 1 from ticket where id_dispositivo = ? and estado = 'EN_PROCESO') then 'EN_PROCESO'
                when exists (select 1 from ticket where id_dispositivo = ? and estado = 'PENDIENTE') then 'PENDIENTE'
                when exists (select 1 from ticket where id_dispositivo = ?) then 'COMPLETADO'
                else 'PENDIENTE' end
            where id_dispositivo = ?
            """;

    static String nextFolio(Connection connection) throws SQLException {
        String prefix = AppConfig.FOLIO_PREFIX + "-" + Year.now().getValue() + "-";
        String sql = "select coalesce(max(cast(substr(folio, ?) as integer)), 0) + 1 from ticket where folio like ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, prefix.length() + 1);
            statement.setString(2, prefix + "%");
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return prefix + String.format("%04d", result.getInt(1));
            }
        }
    }

    @Override
    public List<Ticket> findAll() {
        String sql = SELECT + " order by k.fecha_ingreso desc, k.id_ticket desc";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Ticket> tickets = new ArrayList<>();
            while (result.next()) {
                tickets.add(map(result));
            }
            return tickets;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cargar la lista de tickets.", e);
        }
    }

    @Override
    public Optional<Ticket> findById(int id) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            return findById(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el ticket.", e);
        }
    }

    @Override
    public Ticket save(Ticket ticket) {
        String sql = "insert into ticket (id_dispositivo, id_tecnico, folio, descripcion) values (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, ticket.getDispositivoId());
                    if (ticket.getTecnicoId() == null) {
                        statement.setNull(2, Types.INTEGER);
                    } else {
                        statement.setInt(2, ticket.getTecnicoId());
                    }
                    statement.setString(3, nextFolio(connection));
                    statement.setString(4, ticket.getDescripcion());
                    statement.executeUpdate();
                }
                int id = DatabaseConnection.lastInsertId(connection);
                syncDeviceStatus(connection, ticket.getDispositivoId());
                Ticket saved = findById(connection, id).orElseThrow();
                connection.commit();
                return saved;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo crear el ticket.", e);
        }
    }

    @Override
    public void updateStatus(int id, EstadoServicio status) {
        String sql = """
                update ticket set estado = ?,
                    fecha_completado = case when ? = 'COMPLETADO' then datetime('now', 'localtime') else null end
                where id_ticket = ?
                """;
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, status.name());
                    statement.setString(2, status.name());
                    statement.setInt(3, id);
                    statement.executeUpdate();
                }
                int deviceId = findById(connection, id).orElseThrow().getDispositivoId();
                syncDeviceStatus(connection, deviceId);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cambiar el estado del ticket.", e);
        }
    }

    @Override
    public void markPrinted(int id, Integer tecnicoId) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "update ticket set impreso = 1, fecha_impresion = datetime('now', 'localtime') where id_ticket = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                for (String type : List.of("EQUIPO", "CLIENTE")) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "insert into impresion_ticket (id_ticket, tipo, id_tecnico) values (?, ?, ?)")) {
                        statement.setInt(1, id);
                        statement.setString(2, type);
                        if (tecnicoId == null) {
                            statement.setNull(3, Types.INTEGER);
                        } else {
                            statement.setInt(3, tecnicoId);
                        }
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar la impresión del ticket.", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<Ticket> ticket = findById(connection, id);
                try (PreparedStatement statement = connection.prepareStatement("delete from ticket where id_ticket = ?")) {
                    statement.setInt(1, id);
                    statement.executeUpdate();
                }
                if (ticket.isPresent()) {
                    syncDeviceStatus(connection, ticket.get().getDispositivoId());
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el ticket.", e);
        }
    }

    private void syncDeviceStatus(Connection connection, int deviceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SYNC_DEVICE_STATUS)) {
            for (int position = 1; position <= 4; position++) {
                statement.setInt(position, deviceId);
            }
            statement.executeUpdate();
        }
    }

    private Optional<Ticket> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " where k.id_ticket = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    private Ticket map(ResultSet result) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(result.getInt("id_ticket"));
        ticket.setDispositivoId(result.getInt("id_dispositivo"));
        int technicianId = result.getInt("id_tecnico");
        ticket.setTecnicoId(result.wasNull() ? null : technicianId);
        ticket.setFolio(result.getString("folio"));
        ticket.setDescripcion(result.getString("descripcion"));
        ticket.setEstado(EstadoServicio.valueOf(result.getString("estado")));
        ticket.setFechaIngreso(DatabaseConnection.toDateTime(result.getString("fecha_ingreso")));
        ticket.setFechaCompletado(DatabaseConnection.toDateTime(result.getString("fecha_completado")));
        ticket.setImpreso(result.getInt("impreso") == 1);
        ticket.setFechaImpresion(DatabaseConnection.toDateTime(result.getString("fecha_impresion")));
        ticket.setDispositivoResumen(result.getString("tipo") + " " + result.getString("marca") + " " + result.getString("modelo"));
        ticket.setCliente(result.getString("cliente"));
        ticket.setTecnicoNombre(result.getString("tecnico_nombre"));
        return ticket;
    }
}
