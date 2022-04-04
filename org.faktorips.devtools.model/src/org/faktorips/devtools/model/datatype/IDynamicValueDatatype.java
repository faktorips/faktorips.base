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

import java.util.Currency;

import org.faktorips.datatype.NamedDatatype;
import org.w3c.dom.Element;

public interface IDynamicValueDatatype extends NamedDatatype {

    void setAdaptedClassName(String className);

    void setAdaptedClass(Class<?> clazz);

    String getAdaptedClassName();

    Class<?> getAdaptedClass();

    void writeToXml(Element element);

    /**
     * Sets the name of the method that returns the name of a value of the enumeration class wrapped
     * by this dynamic enum datatype.
     */
    void setGetNameMethodName(String getNameMethodName);

    /**
     * Returns the name of the method returning the value's name.
     */
    String getGetNameMethodName();

    /**
     * If the datatype supports getting the name for an instance, e.g. showing the € symbol for an
     * EUR instance of {@link Currency}
     * 
     * @param supporting {@code true} if the datatype supports getting a name
     */
    void setIsSupportingNames(boolean supporting);

    /**
     * Returns the name of the method that finds the id by name, e.g. finding the {@link Currency}
     * instance for EUR with the € symbol.
     * 
     * @return the corresponding id
     */
    String getGetIdByNameMethodName();

    /**
     * Finds the id by name, e.g. finding the {@link Currency} instance for EUR with the € symbol.
     * 
     * @param name the name
     */
    void setGetIdByNameMethodName(String name);
}
