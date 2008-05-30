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
 * Interface indicating that it is possible to compute a delta between two instances of the class
 * implementing this interface.  
 * 
 * <p><strong>
 * The delta support is experimental in this version.
 * The API might change without notice until it is finalized in a future version.
 * </strong>
 * 
 * @author Jan Ortmann
 */
public interface IDeltaSupport {

    /**
     * Computes a delta between this object and the given other object.
     * 
     * @param otherObject The object this one is compared too.
     * 
     * @throws ClassCastException if otherObject is not an instance of the same class as 'this'.
     * @throws NullPointerException if otherObject is <code>null</code>.
     */
    public IModelObjectDelta computeDelta(IModelObject otherObject, IDeltaComputationOptions options);
    
}
