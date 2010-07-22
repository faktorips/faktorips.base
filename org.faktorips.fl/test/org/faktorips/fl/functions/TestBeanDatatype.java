/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
