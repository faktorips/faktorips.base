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
     * Returns the ID of the container type.
     */
    public String getContainerTypeId();

    /**
     * Returns the IPS object path container that is referenced by this entry. The container is used
     * to resolve the entries that contain IPS objects. Returns <code>null</code> if the container
     * is not found.
     */
    public IIpsObjectPathContainer getIpsObjectPathContainer();

    /**
     * Returns the optional path information needed by certain IPS object path containers to resolve
     * entries. For example the JDT {@link IClasspathContainer} identifies the container by a path
     * {@link IClasspathContainer#getPath()}.
     * 
     * The name is chosen with regards to JDT classpath containers. Note that the first segment in
     * the path returned by the JDT, is the same as the container type. So this path here represents
     * the optional segments,
     */
    public String getOptionalPath();

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
