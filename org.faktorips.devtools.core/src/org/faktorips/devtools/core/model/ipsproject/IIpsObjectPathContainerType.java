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

/**
 * Represents an IPS object path container type. While IPS object path container exist per project,
 * (or to be precise: per IPS object path entry, although there are rarely two containers of the
 * same container type) a container type exists only once in an IPS model.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectPathContainerType {

    /**
     * Returns the container type's id that is unique in the IPS model.
     */
    String getId();

    /**
     * Creates and initializes a new container for the given project.
     * 
     * @param ipsProject The IPS project.
     * @param optionalPath An optional path.
     * 
     * @return The new container.
     * 
     * @throws NullPointerException if ipsProject or optionalPath is <code>null</code>.
     */
    IIpsObjectPathContainer newContainer(IIpsProject ipsProject, String optionalPath);

}
