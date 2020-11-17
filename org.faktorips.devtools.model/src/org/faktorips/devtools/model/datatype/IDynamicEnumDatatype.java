/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.datatype;

import org.faktorips.datatype.EnumDatatype;

public interface IDynamicEnumDatatype extends IDynamicValueDatatype, EnumDatatype {

    /**
     * Sets the name of the method that provides all values of the datatype.
     */
    void setAllValuesMethodName(String getAllValuesMethodName);

    /**
     * Returns the name of the method that provides all values of the datatype.
     */
    String getAllValuesMethodName();

    /**
     * Sets the name of the method that returns the isSupportingNames flag of the enumeration class
     * wrapped by this dynamic enum datatype.
     */
    void setIsSupportingNames(boolean supporting);

    /**
     * Sets the name of the method that returns the name of a value of the enumeration class wrapped
     * by this dynamic enum datatype.
     */
    void setGetNameMethodName(String getNameMethodName);

    /**
     * Returns the name of the method returning the value's name.
     */
    String getGetNameMethodName();

}