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

package org.faktorips.datatype;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for datatypes representing a Java primtive like boolean.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractPrimitiveDatatype extends AbstractDatatype implements ValueDatatype {

    public AbstractPrimitiveDatatype() {
        super();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return true;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isValueDatatype()
     */
    public boolean isValueDatatype() {
        return true;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#valueToXmlString(java.lang.Object)
     */
    public String valueToString(Object value) {
        return "" + value;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isNull(java.lang.Object)
     */
    public boolean isNull(Object value) {
        return false;
    }
    
    /**
     * If the value is <code>null</code> or an empty string, <code>false</code> is
     * returned.
     * 
     * {@inheritDoc}
     */
    public boolean isParsable(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        try {
            getValue(value);
            return true;
            
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
