/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsproject;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

/**
 * An object path entry for an IPS archive.
 * 
 * @author Jan Ortmann
 */
public interface IIpsArchiveEntry extends IIpsObjectPathEntry {

    public final static String FILE_EXTENSION = "ipsar"; //$NON-NLS-1$

    /**
     * Returns the IPS archive this entry refers to.
     */
    public IIpsArchive getIpsArchive();

    /**
     * Returns the archive path. Note that the underlying file might not exist and the file might
     * exists outside the workspace.
     */
    public IPath getArchivePath();

    /**
     * Sets the new archive path.
     */
    public void setArchivePath(IIpsProject ipsProject, IPath archiveFile);

    /**
     * Returns true if a representation of this entry is part of the provided delta or one of its
     * children.
     * <p>
     * Note: For changes of files outside the workspace which are referenced from the project in any
     * kind no IResourceDelta will be created.
     */
    public boolean isContained(IResourceDelta delta);

}
