/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

    /**
     * {@inheritDoc}
     */
    public Datatype getDatatype() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getGetterMethod() {
        return "get" + StringUtils.capitalize(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String getQualifiedName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAbstract() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String getJavaClassName() {
        return null;
    }

}
