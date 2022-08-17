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

import org.eclipse.core.runtime.IPath;
import org.faktorips.runtime.MessageList;

/**
 * An IPS object path container provides a way to wrap a set of IPS object path entries of type
 * archive or project. What entries the container contains is not fixed, it is computed dynamically
 * by the {@link #resolveEntries()} method. Basically the concept is the same as classpath
 * containers in JDT. As JDT containers, IPS object path containers exist per project. (In the JDT
 * documentation is is sometime not clear if they refer to a container type (like JRE runtime,
 * MAVEN, PluginDependencies) or the container instance that exists per project.
 * 
 * <p>
 * To use an IPS object path container in an IPS project, the project's IPS object path must contain
 * an {@link IIpsContainerEntry} that refers to an IPS object path container by container type id (
 * {@link IIpsObjectPathContainerType#getId()}.
 * 
 * @since 3.3
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathContainer {

    /**
     * Returns the IPS project this container belongs to.
     */
    IIpsProject getIpsProject();

    /**
     * Returns the container's optional path information. Never returns null.
     */
    IPath getOptionalPath();

    /**
     * Returns the ID of the container provided by the {@link IIpsObjectPathContainerType}. The ID
     * could be used to identify this container and it cannot change during life cycle.
     * 
     * @return The identifier of this container according to its {@link IIpsObjectPathContainerType}
     */
    String getContainerId();

    /**
     * Returns a name for the given entry that can be presented to the user. E.g. for containers
     * based on a JDT classpath container, the name of the JDT classpath container is returned.
     */
    String getName();

    /**
     * Returns the list of entries of type {@link IIpsObjectPathEntry#TYPE_PROJECT_REFERENCE} or
     * {@link IIpsObjectPathEntry#TYPE_ARCHIVE} that are provided by this container for the given
     * container entry.
     * 
     * @return The resolved list of entries.
     * 
     * @throws NullPointerException if containerEntry is <code>null</code>.
     */
    List<IIpsObjectPathEntry> resolveEntries();

    /**
     * Validates if the given entry is valid.
     * 
     * @return list of messages or <code>null</code>.
     * 
     * @throws NullPointerException if containerEntry is <code>null</code>.
     */
    MessageList validate();
}
