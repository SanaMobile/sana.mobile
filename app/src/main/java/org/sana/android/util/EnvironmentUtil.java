package org.sana.android.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Utility methods for accessing information about the Environment
 *
 * @author Sana Development.
 */
public class EnvironmentUtil {

    /**
     * Gets the absolute path to a publicly accessible directory for adding
     * new procedures. The current implementation returns the
     * {@link android.os.Environment#DIRECTORY_DOWNLOADS
     * Environment.DIRECTORY_DOWNLOADS} location.
     *
     * @return An absolute file path to the directory which will be checked for
     *  new procedures
     */
    public static String getProcedureDirectory(){
        File dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        return dir.getAbsolutePath();
    }
}
