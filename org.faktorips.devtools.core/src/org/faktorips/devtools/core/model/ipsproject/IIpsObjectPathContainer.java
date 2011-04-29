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
 * An ips object path container provides a way to wrap a set of ips object path entries of type
 * archive or project. What entries the container contains is not fixed, it is computed dynamically
 * by the {@link #resolveEntries(IIpsContainerEntry)} method. Basically the concept is the same as
 * classpath containers in JDT.
 * <p>
 * To use an ips object path container in an ips project, the project's ips object path must contain
 * an {@link IIpsContainerEntry} that refers to an ips object path container by id.
 * 
 * @since 3.3
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathContainer {

    /**
     * Returns the kind of the ips object path container that uniquely identifies it in the ips
     * model.
     */
    public String getKind();

    /**
     * Returns a name for the given entry that can be presented to the user. E.g. for containers
     * based on a JDT classpath container, the name of the classpath container is returned.
     */
    public String getName(IIpsContainerEntry entry);

    /**
     * Returns the list of entries of type {@link IIpsObjectPathEntry#TYPE_PROJECT_REFERENCE} or
     * {@link IIpsObjectPathEntry#TYPE_ARCHIVE} that are provided by this container for the given
     * container entry.
     * 
     * @param containerEntry A container entry that refers to this container.
     * @return The resolved list of entries.
     * 
     * @throws CoreException if an exceptions occurs while resolving the entries.
     */
    public List<IIpsObjectPathEntry> resolveEntries(IIpsContainerEntry containerEntry) throws CoreException;

    /**
     * Validates if the given entry is valid.
     * 
     * @return list of messages or <code>null</code>.
     */
    public MessageList validate(IIpsContainerEntry entry) throws CoreException;
}
