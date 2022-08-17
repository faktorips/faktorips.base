/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    void writeFile(DocumentationContext context, String relativPath, byte[] content) throws IOException;

    /**
     * writes the content into a file with the given filename.
     * 
     * 
     */
    void writeFile(String filename, byte[] content) throws IOException;

    /**
     * Reads the content of a file from the given bundle
     * 
     * @param fileName name of the file
     * 
     * @return content of the file
     * @throws IOException if an IOException occurs
     */
    byte[] readFile(String fileName) throws IOException;

}
