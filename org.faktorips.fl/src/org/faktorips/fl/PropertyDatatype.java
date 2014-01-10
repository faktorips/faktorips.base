/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.datatype.Datatype;

/**
 * A datatype representing a bean's property.
 * 
 * @author Jan Ortmann
 */
public interface PropertyDatatype extends Datatype {

    public Datatype getDatatype();

    /**
     * Returns a Java sourcecode fragment to access the property.
     */
    public String getGetterMethod();

}
