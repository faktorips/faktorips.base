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

import org.faktorips.util.message.MessageList;

/**
 * Abstract super class for Datatype implementations.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDatatype implements Datatype {

    /**
     * {@inheritDoc}
     */
    public MessageList checkReadyToUse() {
        return new MessageList();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVoid() {
        return false;
    }

    public boolean isEnum() {
        return this instanceof EnumDatatype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Datatype)) {
            return false;
        }
        return getQualifiedName().equals(((Datatype)o).getQualifiedName());
    }

    /**
     * Returns the type's name.
     */
    @Override
    public String toString() {
        return getQualifiedName();
    }

    /**
     * Compares the two type's alphabetically by their name.
     */
    public int compareTo(Datatype o) {
        Datatype type = (Datatype)o;
        return getQualifiedName().compareTo(type.getQualifiedName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNullObject() {
        return false;
    }

}
