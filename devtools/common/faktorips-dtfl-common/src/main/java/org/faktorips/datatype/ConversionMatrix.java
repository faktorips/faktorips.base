/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * A <code>ConversionMatric</code> holds the information if the value of a datatype can be converted
 * into the value of another datatype.
 */
public interface ConversionMatrix {

    /**
     * Returns true if a value of datatype from can be converted into one of datatype to. If
     * datatype from and to are equal, the method returns true.
     * 
     * @throws IllegalArgumentException if either from or to is null.
     */
    boolean canConvert(Datatype from, Datatype to);

}
