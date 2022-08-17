/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.List;

/**
 * A container entry is an entry that wraps several other, none-container entries. The entries are
 * determined dynamically by the {@link IIpsObjectPathContainer} the entry refers to.
 * 
 * @since 3.3
 */
public interface IIpsContainerEntry extends IIpsObjectPathEntry {

    String MSG_CODE_INVALID_CONTAINER_ENTRY = "IpsContainerEntry-InvalidContainerEntry"; //$NON-NLS-1$

    /**
     * Returns a human readable name for the container entry.
     */
    String getName();

    /**
     * Returns the ID of the container type.
     */
    String getContainerTypeId();

    /**
     * Returns the IPS object path container that is referenced by this entry. The container is used
     * to resolve the entries that contain IPS objects. Returns <code>null</code> if the container
     * is not found.
     */
    IIpsObjectPathContainer getIpsObjectPathContainer();

    /**
     * Returns the optional path information needed by certain IPS object path containers to resolve
     * entries. For example the JDT classpath container identifies the container by a path.
     * 
     * The name is chosen with regards to JDT classpath containers. Note that the first segment in
     * the path returned by the JDT, is the same as the container type. So this path here represents
     * the optional segments,
     */
    String getOptionalPath();

    /**
     * Returns the list of entries that are provided by the container this entry refers to. The
     * returned list does does not contain any container entries.
     */
    List<IIpsObjectPathEntry> resolveEntries();

    /**
     * Returns the resolved entry that defines the given IPS package fragment root. Returns
     * <code>null</code> if not found. So basically this method loops over {@link #resolveEntries()}
     * and checks if one the entries has the given <code>rootName</code>.
     * 
     * @param rootName The name of the IPS package fragment root to search the entry for
     */
    IIpsObjectPathEntry getResolvedEntry(String rootName);

}
