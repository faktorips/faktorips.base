/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFile;

/**
 * An object path entry for an ips archive.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchiveEntry extends IIpsObjectPathEntry {

    public final static String FILE_EXTENSION = "ipsar";
    
    /**
     * Returns the ips archive this entry refers to. 
     */
    public IIpsArchive getIpsArchive();
    
    /**
     * Returns the archive file. Note that the file might not exist.
     */
    public IFile getArchiveFile();
    
    /**
     * Sets the new archive file.
     */
    public void setArchiveFile(IFile archiveFile);
    
    
}
