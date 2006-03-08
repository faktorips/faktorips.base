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

package org.faktorips.devtools.core.model;


/**
 * An interface that marks an object as having a description.
 * 
 * @author Jan Ortmann
 */
public interface Described {
    
    /**
     * Sets the description.
     * 
     * @throws IllegalArgumentException if newDescription is null.
     */
    public abstract void setDescription(String newDescription);
    
    /**
     * Returns the object's description. This method never returns null.
     */
    public abstract String getDescription();

}
