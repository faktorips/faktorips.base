package org.faktorips.devtools.htmlexport.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public abstract class AbstractDocumentorWriter implements DocumentorWriter {
    
    public final void write(DocumentorConfiguration config, IIpsElement element) throws IOException {
        String path = createFilePath(config, element);
        String content = createFileContent(config, element);
        writeFile(config, path, content);
    }

    protected abstract String createFileContent(DocumentorConfiguration config, IIpsElement srcFile);
    protected abstract String createFilePath(DocumentorConfiguration config, IIpsElement srcFile);
    
    protected void writeFile(DocumentorConfiguration config, String filePath, String content) throws IOException {
        FileWriter writer = new FileWriter(config.getPath() + File.separator + filePath);
        writer.append(content);
        writer.close();
    }
}
