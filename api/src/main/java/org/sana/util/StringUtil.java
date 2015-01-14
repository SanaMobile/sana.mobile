package org.sana.util;

/**
 * Utility methods for dealing with String Objects.
 * @author Sana Development Team
 */
public class StringUtil {
    
    /**
     * Formats a patient's name to it's display format.
     * @param firstName The patient's first name.
     * @param lastName The patient's last name.
     * @return The formatted patient name.
     */
    public static String formatPatientDisplayName(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstName);
        sb.append(" ");
        sb.append(lastName);
        return sb.toString();
    }
}
