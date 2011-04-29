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

package org.faktorips.devtools.core.model.ipsproject;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathContainer;
import org.faktorips.devtools.core.model.IIpsModel;

/**
 * A container entry is an entry that wraps severall other, none-container entries. The entries are
 * determined dynamically by the {@link IIpsObjectPathContainer} the entry refers to.
 * 
 * @since 3.3
 */
public interface IIpsContainerEntry extends IIpsObjectPathEntry {

    /**
     * Returns a human readable name for the container entry.
     */
    public String getName();

    /**
     * Returns the kind of the container. The kind is used to lookup the container in the ips model
     * via {@link IIpsModel#getIpsObjectPathContainer(IIpsProject, String)}.
     */
    public String getContainerKind();

    /**
     * Returns the ips object path container that is referenced by this entry. The container is used
     * to resolve the entries that contain ips objects. Returns <code>null</code> if the container
     * is not found.
     */
    public IIpsObjectPathContainer getIpsObjectPathContainer();

    /**
     * Returns the optional path information needed by certain ips object path containers to resolve
     * entries. For exmple the JDT {@link IClasspathContainer} identifies the container by a path
     * {@link IClasspathContainer#getPath()}.
     */
    public String getContainerPath();

    /**
     * Returns the list of entries that are provided by the container this entry refers to. The
     * returned list does does not contain any container entries.
     */
    public List<IIpsObjectPathEntry> resolveEntries() throws CoreException;

    /**
     * Returns the resolved entry that defines the given IPS package fragment root. Returns
     * <code>null</code> if not found. So basically this method loops over {@link #resolveEntries()}
     * and checks if one the entries has the given <code>rootName</code>.
     * 
     * @param rootName The name of the IPS package fragment root to search the entry for
     */
    public IIpsObjectPathEntry getResolvedEntry(String rootName);

}
