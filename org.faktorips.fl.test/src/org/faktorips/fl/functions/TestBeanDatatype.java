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

package org.faktorips.fl.functions;

import java.util.HashMap;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.fl.BeanDatatype;
import org.faktorips.fl.PropertyDatatype;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * Implementation of BeanDatatype for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class TestBeanDatatype extends AbstractDatatype implements BeanDatatype {

    private String name;
    private String javaClassName;
    private HashMap<String, PropertyDatatype> properties = new HashMap<String, PropertyDatatype>();

    public TestBeanDatatype(String javaClassName) {
        ArgumentCheck.notNull(javaClassName);
        this.javaClassName = javaClassName;
        this.name = StringUtil.unqualifiedName(javaClassName);
    }

    public void add(PropertyDatatype property) {
        properties.put(property.getName(), property);
    }

    /**
     * {@inheritDoc}
     */
    public PropertyDatatype getProperty(String name) {
        return properties.get(name);
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
        return javaClassName;
    }

}
