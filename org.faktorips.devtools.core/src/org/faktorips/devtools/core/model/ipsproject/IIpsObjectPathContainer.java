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
import org.faktorips.util.message.MessageList;

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
     * Returns the type of this container. This method never returns <code>null</code>.
     */
    IIpsObjectPathContainerType getContainerType();

    /**
     * Returns the IPS project this container belongs to.
     */
    public IIpsProject getIpsProject();

    /**
     * Returns the container's optional path information. Never returns null.
     */
    public String getOptionalPath();

    /**
     * Returns a name for the given entry that can be presented to the user. E.g. for containers
     * based on a JDT classpath container, the name of the JDT classpath container is returned.
     */
    public String getName();

    /**
     * Returns the list of entries of type {@link IIpsObjectPathEntry#TYPE_PROJECT_REFERENCE} or
     * {@link IIpsObjectPathEntry#TYPE_ARCHIVE} that are provided by this container for the given
     * container entry.
     * 
     * @return The resolved list of entries.
     * 
     * @throws CoreException if an exceptions occurs while resolving the entries.
     * @throws NullPointerException if containerEntry is <code>null</code>.
     */
    public List<IIpsObjectPathEntry> resolveEntries() throws CoreException;

    /**
     * Validates if the given entry is valid.
     * 
     * @return list of messages or <code>null</code>.
     * 
     * @throws NullPointerException if containerEntry is <code>null</code>.
     */
    public MessageList validate() throws CoreException;
}
