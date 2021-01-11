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

import org.faktorips.datatype.Datatype;

/**
 * A datatype representing a Java bean with it's properties.
 * 
 * @author Jan Ortmann
 */
public interface BeanDatatype extends Datatype {

    /**
     * Returns the property datatype representing the property with the given name.
     */

    public PropertyDatatype getProperty(String name);

}
