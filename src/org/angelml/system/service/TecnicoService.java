package org.angelml.system.service;

import org.angelml.system.config.AppConfig;
import org.angelml.system.model.Rol;
import org.angelml.system.model.Tecnico;
import org.angelml.system.repository.TecnicoInterface;
import org.angelml.system.repository.TecnicoRepository;
import org.angelml.system.utils.ValidationsUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

public class TecnicoService {

    private static final int ITERATIONS = 210_000;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TecnicoInterface repository;

    public TecnicoService() {
        this(new TecnicoRepository());
    }

    public TecnicoService(TecnicoInterface repository) {
        this.repository = repository;
    }

    public boolean hasTechnicians() {
        return repository.count() > 0;
    }

    public List<Tecnico> getAll() {
        return repository.findAll();
    }

    public List<Tecnico> getActive() {
        return repository.findAll().stream().filter(Tecnico::isActivo).toList();
    }

    public Tecnico authenticate(String email, String password) {
        if (ValidationsUtils.isBlank(email) || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Escribe tu correo y tu contraseña.");
        }
        Tecnico tecnico = repository.findByEmail(email.trim())
                .filter(found -> verify(password, found.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos."));
        if (!tecnico.isActivo()) {
            throw new IllegalArgumentException("Este técnico está desactivado. Pide a un administrador que lo reactive.");
        }
        return tecnico;
    }

    public Tecnico createFirstAdmin(String nombre, String email, String password) {
        if (hasTechnicians()) {
            throw new IllegalStateException("Ya existe un administrador registrado.");
        }
        Tecnico admin = new Tecnico();
        admin.setNombre(nombre);
        admin.setEmail(email);
        admin.setRol(Rol.ADMIN);
        return create(admin, password);
    }

    public Tecnico create(Tecnico tecnico, String password) {
        validate(tecnico);
        validatePassword(password);
        tecnico.setPassword(hash(password));
        tecnico.setId(repository.save(tecnico));
        return tecnico;
    }

    public void update(Tecnico tecnico, String newPassword) {
        validate(tecnico);
        Tecnico current = repository.findById(tecnico.getId())
                .orElseThrow(() -> new IllegalArgumentException("El técnico ya no existe."));
        boolean losesAdmin = current.isAdmin() && current.isActivo() && (!tecnico.isAdmin() || !tecnico.isActivo());
        if (losesAdmin && repository.countActiveAdmins() <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador activo.");
        }
        if (!ValidationsUtils.isBlank(newPassword)) {
            validatePassword(newPassword);
            tecnico.setPassword(hash(newPassword));
        } else {
            tecnico.setPassword(null);
        }
        repository.update(tecnico);
    }

    public void setActive(int id, boolean active) {
        Tecnico current = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El técnico ya no existe."));
        if (!active && current.isAdmin() && current.isActivo() && repository.countActiveAdmins() <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador activo.");
        }
        repository.setActive(id, active);
    }

    private void validate(Tecnico tecnico) {
        tecnico.setNombre(ValidationsUtils.requireText(tecnico.getNombre(), "nombre", 100));
        String email = ValidationsUtils.requireText(tecnico.getEmail(), "correo", 120);
        if (!ValidationsUtils.validateEmail(email)) {
            throw new IllegalArgumentException("El correo no tiene un formato válido (ej. nombre@empresa.com).");
        }
        tecnico.setEmail(email.toLowerCase());
        if (tecnico.getRol() == null) {
            throw new IllegalArgumentException("Selecciona un rol.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < AppConfig.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + AppConfig.MIN_PASSWORD_LENGTH + " caracteres.");
        }
    }

    private String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    private boolean verify(String password, String stored) {
        if (stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !parts[0].equals("pbkdf2")) {
            return false;
        }
        try {
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, Base64.getDecoder().decode(parts[2]), Integer.parseInt(parts[1]));
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Este equipo no soporta el cifrado de contraseñas.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
