/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.PropertyDatatype;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of PropertyDatatype for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class TestPropertyDatatype extends AbstractDatatype implements PropertyDatatype {

    private String name;

    public TestPropertyDatatype(String name, Datatype datatype) {
        ArgumentCheck.notNull(name);
        ArgumentCheck.notNull(datatype);
        this.name = name;
    }

    @Override
    public Datatype getDatatype() {
        return null;
    }

    @Override
    public String getGetterMethod() {
        return "get" + StringUtils.capitalize(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getQualifiedName() {
        return name;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isValueDatatype() {
        return false;
    }

}
