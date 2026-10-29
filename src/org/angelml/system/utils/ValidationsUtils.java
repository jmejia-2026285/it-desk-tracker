package org.angelml.system.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidationsUtils {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\- ]{7,20}$");

    private ValidationsUtils() {
    }

    public static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    public static String trimToNull(String text) {
        return isBlank(text) ? null : text.trim();
    }

    public static boolean exceeds(String text, int maxLength) {
        return text != null && text.trim().length() > maxLength;
    }

    public static boolean validateEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean validatePhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    public static String requireText(String value, String field, int maxLength) {
        if (isBlank(value)) {
            throw new IllegalArgumentException("Completa el campo «" + field + "».");
        }
        return limitText(value, field, maxLength);
    }

    public static String limitText(String value, String field, int maxLength) {
        if (exceeds(value, maxLength)) {
            throw new IllegalArgumentException("El campo «" + field + "» no puede pasar de " + maxLength + " caracteres.");
        }
        return trimToNull(value);
    }
}
