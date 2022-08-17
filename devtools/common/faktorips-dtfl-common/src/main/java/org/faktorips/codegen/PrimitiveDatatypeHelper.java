/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

/**
 * Extended DatatypeHelper variant for data types representing Java's primitives.
 */
public interface PrimitiveDatatypeHelper extends DatatypeHelper {

    /**
     * Given a JavaCodeFragment containing an expression of the primitive type this is a helper for,
     * returns a JavaCodeFragment that converts the given expression to the appropriate wrapper
     * class.
     * 
     * @throws IllegalArgumentException if expression is null.
     */
    JavaCodeFragment toWrapper(JavaCodeFragment expression);

    /**
     * Retrieves the {@link DatatypeHelper} for the wrapper type of the primitive type for which
     * this helper is responsible.
     * 
     * @return The {@link DatatypeHelper} of the not primitive wrapper type
     */
    DatatypeHelper getWrapperTypeHelper();

}
