/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
    public MessageList validate() throws Exception{
        return new MessageList();
    }

    /**
     * {@inheritDoc}
     */
	public boolean isVoid() {
		return false;
	}

    /**
     * {@inheritDoc}
     */
	public int hashCode() {
	    return getName().hashCode();
	}
	
    /**
     * {@inheritDoc}
     */
	public boolean equals(Object o) {
		if (this==o) {
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
	public String toString() {
		return getQualifiedName();
	}
	
	/**
	 * Compares the two type's alphabetically by their name.
	 */
	public int compareTo(Object o) {
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
