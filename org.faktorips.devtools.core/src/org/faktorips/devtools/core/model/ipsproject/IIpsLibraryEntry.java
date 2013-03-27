/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.IpsBundleEntry;

/**
 * The library entry is the common interface for {@link IIpsArchiveEntry} and
 * {@link IpsBundleEntry}. It is an {@link IIpsObjectPathEntry} for bundles and archives.
 * 
 * 
 * @author dirmeier
 */
public interface IIpsLibraryEntry extends IIpsObjectPathEntry {

    /**
     * Initializes the library and set the specified path.
     * 
     * @throws IOException In case of an exception while IO operations when initializing the library
     */
    public void initStorage(IPath path) throws IOException;

}
