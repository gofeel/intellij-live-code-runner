package net.zzid.backendai;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static String getUnixRelativePath(String base, String path) throws IOException {
        File f = new File(path);
        String normalizedPath = FilenameUtils.normalize(f.getAbsolutePath());
        String rp;

        if(normalizedPath.startsWith(base) && f.exists()) {
            rp = normalizedPath.substring(base.length());
        } else {
            throw new IOException("Invalid file");
        }
        rp = FilenameUtils.separatorsToUnix(rp);
        return rp;
    }
}
