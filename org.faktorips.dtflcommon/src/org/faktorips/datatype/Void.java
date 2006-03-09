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

/**
 * The datatype void representing <code>java.lang.Void</code>.
 *  
 * @author Jan Ortmann
 */
public class Void extends AbstractDatatype implements ValueDatatype {

	public String getName() {
		return "void";
	}
	
    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return "void";
    }

	public boolean isVoid() {
		return true;
	}
	
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#isPrimitive()
     */
    public boolean isPrimitive() {
        return false;
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
     * @see org.faktorips.datatype.Datatype#getWrapperType()
     */
    public Datatype getWrapperType() {
        return null;
    }
	
	public String getJavaClassName() {
		return "void";
	}

	public Object getValue(String value) {
		return null;
	}

	public String valueToXmlString(Object value) {
		return "void";
	}

    /**
     * Overridden Method.
     * @see org.faktorips.datatype.ValueDatatype#valueToString(java.lang.Object)
     */
    public String valueToString(Object value) {
        return "void";
    }
    /**
     * 
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#getValues(java.lang.String[])
     */
    public Object[] getValues(String[] values) {
        Void [] elements = new Void[values.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i]= (Void)getValue(values[i]);
            
        }
        return elements;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isParsable(java.lang.String)
     */
    public boolean isParsable(String value) {
        return false;
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#isNull(java.lang.Object)
     */
    public boolean isNull(Object value) {
        return value==null;
    }

}
