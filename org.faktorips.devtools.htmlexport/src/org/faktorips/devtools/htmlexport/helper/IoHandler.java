/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper;

import java.io.IOException;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;

/**
 * Interface for IO-Operations
 * 
 * @author dicker
 */
public interface IoHandler {

    /**
     * writes the content into a file. The filename is the path of the given
     * {@link DocumentationContext} and relativPath.
     * 
     * 
     */
    public void writeFile(DocumentationContext context, String relativPath, byte[] content) throws IOException;

    /**
     * writes the content into a file with the given filename.
     * 
     * 
     */
    public void writeFile(String filename, byte[] content) throws IOException;

    /**
     * Reads the content of a file from the given bundle
     * 
     * @param bundleName name of the bundle
     * @param fileName name of the file
     * @return content of the file
     * @throws IOException if an IOException occurs
     */
    public byte[] readFile(String bundleName, String fileName) throws IOException;

}