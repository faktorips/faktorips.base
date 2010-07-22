/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Interface indicating that it is possible to compute a delta between two instances of the class
 * implementing this interface.
 * <p>
 * <strong> The delta support is experimental in this version. The API might change without notice
 * until it is finalized in a future version. </strong>
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
