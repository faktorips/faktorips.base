package org.faktorips.devtools.htmlexport.io;

import java.io.IOException;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

public interface DocumentorWriter {

    public void write(DocumentorConfiguration config, IIpsElement element) throws IOException;

}