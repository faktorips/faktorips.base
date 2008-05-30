/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Interface indicating that the model object can create a copy of itself. 
 * 
 * <p><strong>
 * The copy support is experimental in this version.
 * The API might change without notice until it is finalized in a future version.
 * </strong>
 * 
 * @author Jan Ortmann
 */
public interface ICopySupport {

    /**
     * Creates and returns new copy of this object.  
     */
    public IModelObject newCopy();
    
}
