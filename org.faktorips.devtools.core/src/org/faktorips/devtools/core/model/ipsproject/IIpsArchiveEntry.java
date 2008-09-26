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

package org.faktorips.devtools.core.model.ipsproject;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

/**
 * An object path entry for an ips archive.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchiveEntry extends IIpsObjectPathEntry {

    public final static String FILE_EXTENSION = "ipsar"; //$NON-NLS-1$
    
    /**
     * Returns the ips archive this entry refers to. 
     */
    public IIpsArchive getIpsArchive();
    
    /**
     * Returns the archive path. Note that the underlying file might not exist and the file might exists outside the workspace.
     */
    public IPath getArchivePath();
    
    /**
     * Sets the new archive path.
     */
    public void setArchivePath(IIpsProject ipsProject, IPath archiveFile);
    
    /**
     * Returns true if a representation of this entry is part of the provided delta or one of its
     * children. <p/> Note: For changes of files outside the workspace which are referenced from the
     * project in any kind no IResourceDelta will be created.
     */
    public boolean isContained(IResourceDelta delta);
    
    
}
