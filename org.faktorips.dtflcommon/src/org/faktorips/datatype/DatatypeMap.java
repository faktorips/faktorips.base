/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.datatype;

/**
 * A map that stores <code>Datatype</code>s as values and uses their name as the access key.
 * 
 * @author Jan Ortmann
 */
public interface DatatypeMap {

    /**
     * Returns the Datatype with the given name, if the registry contains a datatype with the given
     * name. Returns null, if the map does not contain a datatype with the given name.
     * 
     * @throws IllegalArgumentException if the name is null.
     */
    public abstract Datatype getDatatype(String name) throws IllegalArgumentException;

    /**
     * Returns the datatyppes available in the map.
     */
    public abstract Datatype[] getDatatypes();

}
