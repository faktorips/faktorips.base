/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.datatype.ConversionMatrix;
import org.faktorips.datatype.Datatype;

/**
 * Interface that defines a function's signature. The function signature may contain a variable
 * argument list. If so all arguments must be of the same Datatype. See the description of the
 * getArgTypes() method for more details.
 */
public interface FunctionSignature {

    /**
     * Returns the function's return type.
     */
    public Datatype getType();

    /**
     * Returns the function's name.
     */
    public String getName();

    /**
     * Returns the function's arguments' types. If the function has variable arguments the returned
     * array is of length 1 and contains the Datatype that is valid for all arguments of this
     * function.
     */
    public Datatype[] getArgTypes();

    /**
     * Returns true if the given fctSignature is the same as this one. This is the case if they have
     * the same type, name and the arguments' types are the same (in the same order).
     */
    public boolean isSame(FunctionSignature fctSignature);

    /**
     * Indicates if this function has a variable argument list. If so the return value of the
     * getArgTyes() method is an array of lenght 1 and contains the Datatype that is valid for all
     * arguments of this function.
     */
    public boolean hasVarArgs();

    /**
     * Returns true if this function signature has the indicated name and argument types.
     */
    public boolean match(String name, Datatype[] argTypes);

    /**
     * Returns true if this function signature has the indicated name and if each given argument
     * type is either equal to this function's argument type or can be convert to it.
     */
    public boolean matchUsingConversion(String name, Datatype[] argTypes, ConversionMatrix matrix);

}
