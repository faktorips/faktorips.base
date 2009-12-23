package org.faktorips.devtools.htmlexport.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public class FileHandler {
    public static void writeFile(DocumentorConfiguration config, String filePath, byte[] content) {
        try {
            File file = new File((config.getPath() + File.separator + filePath)); 
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(content);
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Schreiben der Datei", e);
        }
    }
}
