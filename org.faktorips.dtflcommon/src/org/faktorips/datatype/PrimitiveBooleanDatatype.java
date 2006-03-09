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
 * Datatype for the primitive <code>boolean</code>.
 */
public class PrimitiveBooleanDatatype extends AbstractPrimitiveDatatype {

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getName()
     */
    public String getName() {
        return "boolean";
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getQualifiedName()
     */
    public String getQualifiedName() {
        return "boolean";
    }

    /**
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getWrapperType()
     */
    public Datatype getWrapperType() {
        return Datatype.BOOLEAN;
    }
	
    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getJavaClassName()
     */
    public String getJavaClassName() {
        return "boolean";
    }

    /** 
     * Overridden method.
     * @see org.faktorips.datatype.Datatype#getValue(java.lang.String)
     */
    public Object getValue(String value) {
        return Boolean.valueOf(value);
    }

}
