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

package org.faktorips.codegen;

/**
 * Extended DatatypeHelper variant for datatypes representing Java's primitives.
 */
public interface PrimitiveDatatypeHelper extends DatatypeHelper {

    /**
     * Given a JavaCodeFragment containing an expression of the primitive type this is a helper for,
     * returns a JavaCodeFragment that converts the given expression to the appropriate wrapper
     * class.
     * 
     * @throws IllegalArgumentException if expression is null.
     */
    public JavaCodeFragment toWrapper(JavaCodeFragment expression);
}
