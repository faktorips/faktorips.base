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

import org.faktorips.datatype.ValueDatatype;
import org.w3c.dom.Element;

public interface IDynamicValueDatatype extends ValueDatatype {

    void setAdaptedClassName(String className);

    void setAdaptedClass(Class<?> clazz);

    String getAdaptedClassName();

    Class<?> getAdaptedClass();

    void writeToXml(Element element);

}