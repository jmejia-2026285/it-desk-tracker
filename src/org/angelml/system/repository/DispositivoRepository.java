package org.angelml.system.repository;

import org.angelml.system.DB.DatabaseConnection;
import org.angelml.system.model.Dispositivo;
import org.angelml.system.model.EstadoServicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DispositivoRepository implements DispositivoInterface {

    private static final String SELECT = """
            select d.id_dispositivo, d.tipo, d.marca, d.modelo, d.serie, d.cliente, d.telefono, d.correo,
                   d.problematica, d.diagnostico, d.accesorios, d.notas, d.imagen, d.estado, d.fecha_ingreso,
                   d.fecha_estimada, d.id_tecnico_recibe, t.nombre as tecnico_nombre, k.id_ticket, k.folio
            from dispositivo d
            left join tecnico t on t.id_tecnico = d.id_tecnico_recibe
            left join ticket k on k.id_ticket = (select min(id_ticket) from ticket where id_dispositivo = d.id_dispositivo)
            """;

    private static final String INSERT = """
            insert into dispositivo (tipo, marca, modelo, serie, cliente, telefono, correo, problematica,
                                     diagnostico, accesorios, notas, imagen, fecha_estimada, id_tecnico_recibe)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            update dispositivo set tipo = ?, marca = ?, modelo = ?, serie = ?, cliente = ?, telefono = ?, correo = ?,
                                   problematica = ?, diagnostico = ?, accesorios = ?, notas = ?, imagen = ?,
                                   fecha_estimada = ?
            where id_dispositivo = ?
            """;

    @Override
    public List<Dispositivo> findAll() {
        String sql = SELECT + " order by d.fecha_ingreso desc, d.id_dispositivo desc";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Dispositivo> dispositivos = new ArrayList<>();
            while (result.next()) {
                dispositivos.add(map(result));
            }
            return dispositivos;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cargar la lista de dispositivos.", e);
        }
    }

    @Override
    public Optional<Dispositivo> findById(int id) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            return findById(connection, id);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el dispositivo.", e);
        }
    }

    @Override
    public Dispositivo register(Dispositivo dispositivo) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int deviceId = insertDevice(connection, dispositivo);
                insertIntakeTicket(connection, deviceId, dispositivo);
                Dispositivo saved = findById(connection, deviceId).orElseThrow();
                connection.commit();
                return saved;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el dispositivo.", e);
        }
    }

    @Override
    public void update(Dispositivo dispositivo) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, dispositivo.getTipo());
            statement.setString(2, dispositivo.getMarca());
            statement.setString(3, dispositivo.getModelo());
            statement.setString(4, dispositivo.getSerie());
            statement.setString(5, dispositivo.getCliente());
            statement.setString(6, dispositivo.getTelefono());
            statement.setString(7, dispositivo.getCorreo());
            statement.setString(8, dispositivo.getProblematica());
            statement.setString(9, dispositivo.getDiagnostico());
            statement.setString(10, dispositivo.getAccesorios());
            statement.setString(11, dispositivo.getNotas());
            statement.setString(12, dispositivo.getImagen());
            statement.setString(13, dispositivo.getFechaEstimada() == null ? null : dispositivo.getFechaEstimada().toString());
            statement.setInt(14, dispositivo.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el dispositivo.", e);
        }
    }

    @Override
    public void delete(int id) {
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("delete from dispositivo where id_dispositivo = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar el dispositivo.", e);
        }
    }

    @Override
    public Map<EstadoServicio, Integer> countByStatus() {
        Map<EstadoServicio, Integer> counts = new EnumMap<>(EstadoServicio.class);
        for (EstadoServicio status : EstadoServicio.values()) {
            counts.put(status, 0);
        }
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("select estado, count(*) from dispositivo group by estado");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                counts.put(EstadoServicio.valueOf(result.getString(1)), result.getInt(2));
            }
            return counts;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar los dispositivos.", e);
        }
    }

    private int insertDevice(Connection connection, Dispositivo dispositivo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, dispositivo.getTipo());
            statement.setString(2, dispositivo.getMarca());
            statement.setString(3, dispositivo.getModelo());
            statement.setString(4, dispositivo.getSerie());
            statement.setString(5, dispositivo.getCliente());
            statement.setString(6, dispositivo.getTelefono());
            statement.setString(7, dispositivo.getCorreo());
            statement.setString(8, dispositivo.getProblematica());
            statement.setString(9, dispositivo.getDiagnostico());
            statement.setString(10, dispositivo.getAccesorios());
            statement.setString(11, dispositivo.getNotas());
            statement.setString(12, dispositivo.getImagen());
            statement.setString(13, dispositivo.getFechaEstimada() == null ? null : dispositivo.getFechaEstimada().toString());
            if (dispositivo.getTecnicoId() == null) {
                statement.setNull(14, Types.INTEGER);
            } else {
                statement.setInt(14, dispositivo.getTecnicoId());
            }
            statement.executeUpdate();
        }
        return DatabaseConnection.lastInsertId(connection);
    }

    private void insertIntakeTicket(Connection connection, int deviceId, Dispositivo dispositivo) throws SQLException {
        String sql = "insert into ticket (id_dispositivo, id_tecnico, folio, descripcion) values (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, deviceId);
            if (dispositivo.getTecnicoId() == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, dispositivo.getTecnicoId());
            }
            statement.setString(3, TicketRepository.nextFolio(connection));
            statement.setString(4, dispositivo.getProblematica());
            statement.executeUpdate();
        }
    }

    private Optional<Dispositivo> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " where d.id_dispositivo = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    private Dispositivo map(ResultSet result) throws SQLException {
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setId(result.getInt("id_dispositivo"));
        dispositivo.setTipo(result.getString("tipo"));
        dispositivo.setMarca(result.getString("marca"));
        dispositivo.setModelo(result.getString("modelo"));
        dispositivo.setSerie(result.getString("serie"));
        dispositivo.setCliente(result.getString("cliente"));
        dispositivo.setTelefono(result.getString("telefono"));
        dispositivo.setCorreo(result.getString("correo"));
        dispositivo.setProblematica(result.getString("problematica"));
        dispositivo.setDiagnostico(result.getString("diagnostico"));
        dispositivo.setAccesorios(result.getString("accesorios"));
        dispositivo.setNotas(result.getString("notas"));
        dispositivo.setImagen(result.getString("imagen"));
        dispositivo.setEstado(EstadoServicio.valueOf(result.getString("estado")));
        dispositivo.setFechaIngreso(DatabaseConnection.toDateTime(result.getString("fecha_ingreso")));
        dispositivo.setFechaEstimada(DatabaseConnection.toDate(result.getString("fecha_estimada")));
        int technicianId = result.getInt("id_tecnico_recibe");
        dispositivo.setTecnicoId(result.wasNull() ? null : technicianId);
        dispositivo.setTecnicoNombre(result.getString("tecnico_nombre"));
        dispositivo.setTicketId(result.getInt("id_ticket"));
        dispositivo.setFolio(result.getString("folio"));
        return dispositivo;
    }
}
