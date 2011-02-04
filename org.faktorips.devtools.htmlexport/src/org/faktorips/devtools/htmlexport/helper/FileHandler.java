/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.osgi.framework.Bundle;

/**
 * This class writes and read files for the documentator.
 * 
 * @author dicker
 * 
 */
public class FileHandler implements IoHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFile(DocumentationContext context, String relativPath, byte[] content) throws IOException {
        writeFile(context.getPath() + File.separator + relativPath, content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFile(String filename, byte[] content) throws IOException {
        File file = new File((filename));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        OutputStream outputStream = new FileOutputStream(file);
        outputStream.write(content);
        outputStream.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] readFile(String bundleName, String fileName) throws IOException {
        if (Platform.getBundle(bundleName) == null) {
            throw new IOException("Bundle " + bundleName + " not found (Loading file " + fileName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        BufferedInputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Bundle bundle = Platform.getBundle(bundleName);
            URL resource = bundle.getResource(fileName);

            if (resource == null) {
                throw new IOException(fileName + " not found in " + bundleName); //$NON-NLS-1$
            }
            in = new BufferedInputStream(resource.openStream());
            byte[] buffer = new byte[8 * 1024];
            int count;

            while ((count = in.read(buffer)) >= 0) {
                out.write(buffer, 0, count);
            }
            in.close();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return out.toByteArray();
    }
}
