/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
    private HashMap properties = new HashMap();
    
    public TestBeanDatatype(String javaClassName) {
        ArgumentCheck.notNull(javaClassName);
        this.javaClassName = javaClassName;
        this.name = StringUtil.unqualifiedName(javaClassName);
    }
    
    public void add(PropertyDatatype property) {
        properties.put(property.getName(), property);
    }
    
    /**
     * Overridden Method.
     *
     * @see org.faktorips.fl.BeanDatatype#getProperty(java.lang.String)
     */
    public PropertyDatatype getProperty(String name) {
        return (PropertyDatatype)properties.get(name);
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return name;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#isValueDatatype()
     */
    public boolean isValueDatatype() {
        return false;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return javaClassName;
    }

}
